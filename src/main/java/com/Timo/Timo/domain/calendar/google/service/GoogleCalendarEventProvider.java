package com.Timo.Timo.domain.calendar.google.service;

import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;
import com.Timo.Timo.domain.calendar.google.client.GoogleOAuthClient;
import com.Timo.Timo.domain.calendar.google.dto.client.GoogleCalendarEventItem;
import com.Timo.Timo.domain.calendar.google.repository.GoogleCalendarConnectionRepository;
import com.Timo.Timo.domain.calendar.service.CalendarEventProvider;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleCalendarEventProvider implements CalendarEventProvider {

  private final GoogleCalendarConnectionRepository calendarConnectionRepository;
  private final GoogleCalendarTokenService calendarTokenService;
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
        .map(GoogleCalendarEventItem::toExternalEvent)
        .toList();
  }
}
