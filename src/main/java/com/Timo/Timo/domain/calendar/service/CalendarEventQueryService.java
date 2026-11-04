package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;
import com.Timo.Timo.domain.calendar.dto.response.CalendarDayResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarEventResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarEventsResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarSourceFailureResponse;
import com.Timo.Timo.domain.calendar.enums.CalendarFilter;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.calendar.utils.CalendarEventDateResolver;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalendarEventQueryService {

  private static final Comparator<SourcedEvent> START_ORDER = Comparator
      .comparing((SourcedEvent sourced) -> sourced.event().start().dateTime() != null)
      .thenComparing(sourced -> sourced.event().start().dateTime(),
          Comparator.nullsFirst(Comparator.naturalOrder()));

  private final UserRepository userRepository;
  private final CalendarEventProviders calendarEventProviders;

  public CalendarEventsResponse getEvents(Long userId, String filterValue, String baseDateValue) {
    User user = getUser(userId);
    ZoneId userZone = ZoneId.of(user.getZoneId());

    CalendarFilter filter = CalendarFilter.from(filterValue);
    LocalDate baseDate = parseBaseDate(baseDateValue, userZone);
    LocalDate from = filter.rangeStart(baseDate);
    LocalDate to = filter.rangeEnd(baseDate);

    Instant timeMin = from.atStartOfDay(userZone).toInstant();
    Instant timeMax = to.plusDays(1).atStartOfDay(userZone).toInstant();

    Map<LocalDate, List<SourcedEvent>> eventsByDate = new LinkedHashMap<>();
    List<CalendarSourceFailureResponse> failedSources = calendarEventProviders.forEachConnected(userId, provider -> {
      List<ExternalCalendarEvent> events = provider.fetchEvents(userId, timeMin, timeMax);
      groupByDate(eventsByDate, provider.source(), events, from, to, userZone);
    });

    List<CalendarDayResponse> days = from.datesUntil(to.plusDays(1))
        .map(date -> CalendarDayResponse.of(date, toSortedResponses(eventsByDate.getOrDefault(date, List.of()))))
        .toList();

    return CalendarEventsResponse.of(filter, baseDate, days, failedSources);
  }

  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
  }

  private LocalDate parseBaseDate(String baseDateValue, ZoneId userZone) {
    if (baseDateValue == null || baseDateValue.isBlank()) {
      return LocalDate.now(userZone);
    }
    try {
      return LocalDate.parse(baseDateValue);
    } catch (DateTimeParseException exception) {
      throw new CustomException(CalendarErrorCode.INVALID_FILTER_OR_DATE);
    }
  }

  private void groupByDate(
      Map<LocalDate, List<SourcedEvent>> eventsByDate,
      CalendarSource source,
      List<ExternalCalendarEvent> events,
      LocalDate from,
      LocalDate to,
      ZoneId userZone
  ) {
    for (ExternalCalendarEvent event : events) {
      for (LocalDate date : CalendarEventDateResolver.resolveDates(event, userZone)) {
        if (date.isBefore(from) || date.isAfter(to)) {
          continue;
        }
        eventsByDate
            .computeIfAbsent(date, key -> new ArrayList<>())
            .add(new SourcedEvent(event, source));
      }
    }
  }

  // 공급자 순서와 관계없이 같은 날짜 안에서는 종일 일정 먼저, 나머지는 시작 시각순으로 내려준다.
  private List<CalendarEventResponse> toSortedResponses(List<SourcedEvent> events) {
    return events.stream()
        .sorted(START_ORDER)
        .map(sourced -> CalendarEventResponse.of(sourced.event().title(), sourced.source()))
        .toList();
  }

  private record SourcedEvent(ExternalCalendarEvent event, CalendarSource source) {

  }
}
