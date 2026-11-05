package com.Timo.Timo.domain.calendar.apple.utils;

import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;
import com.Timo.Timo.domain.calendar.dto.event.ExternalEventDateTime;
import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.component.VEvent;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ICalendarEventParser {

  public static final String EVENT_ID_PREFIX = "apple:";

  private static final DateTimeFormatter DATE_KEY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final DateTimeFormatter DATE_TIME_KEY_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

  // CalDAV 일정 리소스 하나(같은 UID의 원본 일정과 따로 수정한 회차)를 조회 기간의 공통 일정 목록으로 바꾼다.
  public static List<ExternalCalendarEvent> parse(
      String calendarData, Instant timeMin, Instant timeMax, ZoneId userZone
  ) {
    Calendar calendar;
    try {
      calendar = new CalendarBuilder().build(new StringReader(calendarData));
    } catch (Exception exception) {
      log.warn("애플 일정 iCalendar 파싱 실패, 해당 일정을 건너뜁니다. reason={}", exception.getClass().getSimpleName());
      return List.of();
    }

    Map<String, List<VEvent>> eventsByUid = new LinkedHashMap<>();
    for (Object component : calendar.getComponents()) {
      if (component instanceof VEvent event) {
        findValue(event, Property.UID).ifPresent(uid ->
            eventsByUid.computeIfAbsent(uid, key -> new ArrayList<>()).add(event)
        );
      }
    }

    List<ExternalCalendarEvent> results = new ArrayList<>();
    eventsByUid.forEach((uid, events) -> {
      try {
        results.addAll(toExternalEvents(uid, events, timeMin, timeMax, userZone));
      } catch (RuntimeException exception) {
        log.warn("애플 일정 변환 실패, 해당 일정을 건너뜁니다. reason={}", exception.getClass().getSimpleName());
      }
    });
    return results;
  }

  private static List<ExternalCalendarEvent> toExternalEvents(
      String uid, List<VEvent> events, Instant timeMin, Instant timeMax, ZoneId userZone
  ) {
    VEvent master = null;
    List<VEvent> overrides = new ArrayList<>();
    for (VEvent event : events) {
      if (event.getProperty(Property.RECURRENCE_ID).isPresent()) {
        overrides.add(event);
      } else {
        master = event;
      }
    }

    List<ExternalCalendarEvent> results = new ArrayList<>();
    Set<String> overriddenKeys = new HashSet<>();
    for (VEvent override : overrides) {
      Temporal recurrenceId = findDate(override, Property.RECURRENCE_ID).orElseThrow();
      String recurrenceKey = toRecurrenceKey(recurrenceId, userZone);
      overriddenKeys.add(recurrenceKey);
      if (!isCancelled(override)) {
        toSingleEvent(override, EVENT_ID_PREFIX + uid + "_" + recurrenceKey, userZone).ifPresent(results::add);
      }
    }

    if (master == null || isCancelled(master)) {
      return results;
    }
    if (!isRecurring(master)) {
      toSingleEvent(master, EVENT_ID_PREFIX + uid, userZone).ifPresent(results::add);
      return results;
    }

    Temporal masterStart = findDate(master, Property.DTSTART).orElse(null);
    if (masterStart == null) {
      return results;
    }
    for (Period<Temporal> period : master.<Temporal>calculateRecurrenceSet(toQueryPeriod(masterStart, timeMin, timeMax, userZone))) {
      String recurrenceKey = toRecurrenceKey(period.getStart(), userZone);
      if (overriddenKeys.contains(recurrenceKey)) {
        continue;
      }
      results.add(new ExternalCalendarEvent(
          EVENT_ID_PREFIX + uid + "_" + recurrenceKey,
          findValue(master, Property.SUMMARY).orElse(null),
          toExternalDateTime(period.getStart(), userZone),
          toExternalDateTime(period.getEnd(), userZone)
      ));
    }
    return results;
  }

  private static Optional<ExternalCalendarEvent> toSingleEvent(VEvent event, String eventId, ZoneId userZone) {
    Optional<Temporal> start = findDate(event, Property.DTSTART);
    if (start.isEmpty()) {
      return Optional.empty();
    }
    Temporal end = resolveEnd(event, start.get());
    return Optional.of(new ExternalCalendarEvent(
        eventId,
        findValue(event, Property.SUMMARY).orElse(null),
        toExternalDateTime(start.get(), userZone),
        toExternalDateTime(end, userZone)
    ));
  }

  // DTEND가 없으면 DURATION으로 계산하고, 둘 다 없으면 종일 일정은 하루, 시간 일정은 시작 시각에 끝난 것으로 본다.
  private static Temporal resolveEnd(VEvent event, Temporal start) {
    Optional<Temporal> end = findDate(event, Property.DTEND);
    if (end.isPresent()) {
      return end.get();
    }
    Optional<Duration> duration = event.getProperty(Property.DURATION);
    if (duration.isPresent()) {
      return start.plus(duration.get().getDuration());
    }
    if (start instanceof LocalDate) {
      return start.plus(1, ChronoUnit.DAYS);
    }
    return start;
  }

  // 반복 계산 기간은 DTSTART와 같은 날짜 형식이어야 해서 시작 값의 형식에 맞춘다.
  private static Period<Temporal> toQueryPeriod(Temporal masterStart, Instant timeMin, Instant timeMax, ZoneId userZone) {
    if (masterStart instanceof LocalDate) {
      return new Period<>(
          timeMin.atZone(userZone).toLocalDate().minusDays(1),
          timeMax.atZone(userZone).toLocalDate().plusDays(1)
      );
    }
    if (masterStart instanceof LocalDateTime) {
      return new Period<>(
          LocalDateTime.ofInstant(timeMin, userZone),
          LocalDateTime.ofInstant(timeMax, userZone)
      );
    }
    if (masterStart instanceof ZonedDateTime zonedStart) {
      return new Period<>(timeMin.atZone(zonedStart.getZone()), timeMax.atZone(zonedStart.getZone()));
    }
    if (masterStart instanceof OffsetDateTime offsetStart) {
      return new Period<>(timeMin.atOffset(offsetStart.getOffset()), timeMax.atOffset(offsetStart.getOffset()));
    }
    return new Period<>(timeMin, timeMax);
  }

  private static ExternalEventDateTime toExternalDateTime(Temporal temporal, ZoneId userZone) {
    if (temporal instanceof LocalDate date) {
      return ExternalEventDateTime.ofDate(date);
    }
    return ExternalEventDateTime.ofDateTime(toInstant(temporal, userZone));
  }

  private static String toRecurrenceKey(Temporal temporal, ZoneId userZone) {
    if (temporal instanceof LocalDate date) {
      return DATE_KEY_FORMAT.format(date);
    }
    return DATE_TIME_KEY_FORMAT.format(toInstant(temporal, userZone));
  }

  // 시간대 없는(floating) 일정은 사용자 시간대 기준으로 해석한다.
  private static Instant toInstant(Temporal temporal, ZoneId userZone) {
    if (temporal instanceof Instant instant) {
      return instant;
    }
    if (temporal instanceof ZonedDateTime zonedDateTime) {
      return zonedDateTime.toInstant();
    }
    if (temporal instanceof OffsetDateTime offsetDateTime) {
      return offsetDateTime.toInstant();
    }
    if (temporal instanceof LocalDateTime localDateTime) {
      return localDateTime.atZone(userZone).toInstant();
    }
    throw new IllegalArgumentException("지원하지 않는 날짜 형식: " + temporal.getClass().getSimpleName());
  }

  private static boolean isRecurring(VEvent event) {
    return event.getProperty(Property.RRULE).isPresent() || event.getProperty(Property.RDATE).isPresent();
  }

  private static boolean isCancelled(VEvent event) {
    return findValue(event, Property.STATUS)
        .map(Status.VALUE_CANCELLED::equalsIgnoreCase)
        .orElse(false);
  }

  private static Optional<String> findValue(VEvent event, String name) {
    return event.<Property>getProperty(name)
        .map(Property::getValue)
        .filter(value -> !value.isBlank());
  }

  private static Optional<Temporal> findDate(VEvent event, String name) {
    return event.<DateProperty<Temporal>>getProperty(name)
        .map(DateProperty::getDate);
  }
}
