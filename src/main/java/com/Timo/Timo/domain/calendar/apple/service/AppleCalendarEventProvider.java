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
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppleCalendarEventProvider implements CalendarEventProvider {

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

    List<ExternalCalendarEvent> events = new ArrayList<>();
    for (URI calendarUrl : findEventCalendarUrls(userId, connection)) {
      for (String calendarData : appleCalDavClient.fetchCalendarData(appleId, appPassword, calendarUrl, timeMin, timeMax)) {
        events.addAll(ICalendarEventParser.parse(calendarData, timeMin, timeMax, userZone));
      }
    }
    return events;
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
