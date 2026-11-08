package com.Timo.Timo.domain.calendar.apple.service;

import com.Timo.Timo.domain.calendar.apple.client.AppleCalDavClient;
import com.Timo.Timo.domain.calendar.apple.entity.AppleCalendarConnection;
import com.Timo.Timo.domain.calendar.apple.repository.AppleCalendarConnectionRepository;
import com.Timo.Timo.domain.calendar.apple.utils.ICalendarEventParser;
import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.calendar.service.CalendarEventProvider;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import com.Timo.Timo.global.exception.CustomException;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppleCalendarEventProvider implements CalendarEventProvider {

  private static final String PARSE_FAILURE_MESSAGE = "애플 일정 iCalendar 파싱 실패";

  private final AppleCalendarConnectionRepository calendarConnectionRepository;
  private final AppleCalendarConnectionCommandService commandService;
  private final AppleCalDavClient appleCalDavClient;

  @Override
  public CalendarSource source() {
    return CalendarSource.APPLE;
  }

  @Override
  public boolean isConnected(Long userId) {
    return calendarConnectionRepository.existsByUserId(userId);
  }

  @Override
  public List<ExternalCalendarEvent> fetchEvents(Long userId, Instant timeMin, Instant timeMax) {
    AppleCalendarConnection connection = calendarConnectionRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(CalendarErrorCode.APPLE_CALENDAR_NOT_CONNECTED));
    String appleId = connection.getAppleId();
    String appPassword = connection.getAppPassword();
    ZoneId userZone = ZoneId.of(connection.getUser().getZoneId());

    // 공유받은 캘린더의 일정에 초대도 받으면 같은 UID 일정이 여러 캘린더에 있으므로 처음 것만 남긴다.
    Map<String, ExternalCalendarEvent> eventsById = new LinkedHashMap<>();
    for (URI calendarUrl : findEventCalendarUrls(userId, connection)) {
      for (String calendarData : appleCalDavClient.fetchCalendarData(appleId, appPassword, calendarUrl, timeMin, timeMax)) {
        for (ExternalCalendarEvent event : parse(userId, calendarData, timeMin, timeMax, userZone)) {
          eventsById.putIfAbsent(event.eventId(), event);
        }
      }
    }
    return new ArrayList<>(eventsById.values());
  }

  // 깨진 일정이 iCloud에 남아 있으면 그 사용자의 애플 동기화·조회가 계속 실패하므로 Sentry로 알린다.
  // 일정 내용이 섞이지 않도록 예외 대신 고정 메시지와 사용자 ID만 보낸다.
  private List<ExternalCalendarEvent> parse(
      Long userId, String calendarData, Instant timeMin, Instant timeMax, ZoneId userZone
  ) {
    try {
      return ICalendarEventParser.parse(calendarData, timeMin, timeMax, userZone);
    } catch (CustomException exception) {
      Sentry.captureMessage(PARSE_FAILURE_MESSAGE, SentryLevel.WARNING, scope -> {
        scope.setTag("calendarSource", CalendarSource.APPLE.name());
        scope.setTag("userId", String.valueOf(userId));
      });
      throw exception;
    }
  }

  // 저장한 캘린더 홈 주소가 바뀌었으면 다시 찾아 저장한다.
  private List<URI> findEventCalendarUrls(Long userId, AppleCalendarConnection connection) {
    String appleId = connection.getAppleId();
    String appPassword = connection.getAppPassword();
    return appleCalDavClient.findEventCalendarUrls(appleId, appPassword, connection.getCalendarHomeUrl())
        .orElseGet(() -> {
          String calendarHomeUrl = appleCalDavClient.discoverCalendarHomeUrl(appleId, appPassword);
          commandService.updateCalendarHomeUrl(userId, calendarHomeUrl);
          return appleCalDavClient.findEventCalendarUrls(appleId, appPassword, calendarHomeUrl)
              .orElseThrow(() -> new CustomException(CalendarErrorCode.APPLE_CALENDAR_UPSTREAM_ERROR));
        });
  }
}
