package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.client.GoogleOAuthClient;
import com.Timo.Timo.domain.calendar.dto.client.CalendarEventItem;
import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;
import com.Timo.Timo.domain.calendar.repository.CalendarConnectionRepository;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleCalendarEventProvider implements CalendarEventProvider {

  private final CalendarConnectionRepository calendarConnectionRepository;
  private final CalendarTokenService calendarTokenService;
  private final GoogleOAuthClient googleOAuthClient;

  @Override
  public CalendarSource source() {
    return CalendarSource.GOOGLE;
  }

  @Override
  public boolean isConnected(Long userId) {
    return calendarConnectionRepository.existsByUserId(userId);
  }

  @Override
  public List<ExternalCalendarEvent> fetchEvents(Long userId, Instant timeMin, Instant timeMax) {
    String accessToken = calendarTokenService.ensureValidAccessToken(userId);
    return googleOAuthClient.fetchEvents(accessToken, timeMin, timeMax).stream()
        .map(CalendarEventItem::toExternalEvent)
        .toList();
  }
}
