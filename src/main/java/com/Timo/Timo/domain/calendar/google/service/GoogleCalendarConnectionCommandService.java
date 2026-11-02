package com.Timo.Timo.domain.calendar.google.service;

import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.calendar.google.dto.client.GoogleTokenResponse;
import com.Timo.Timo.domain.calendar.google.dto.client.GoogleUserInfoResponse;
import com.Timo.Timo.domain.calendar.google.dto.response.GoogleCalendarConnectResponse;
import com.Timo.Timo.domain.calendar.google.entity.GoogleCalendarConnection;
import com.Timo.Timo.domain.calendar.google.entity.GoogleTokenRevocationOutbox;
import com.Timo.Timo.domain.calendar.google.repository.GoogleCalendarConnectionRepository;
import com.Timo.Timo.domain.calendar.google.repository.GoogleTokenRevocationOutboxRepository;
import com.Timo.Timo.domain.calendar.service.CalendarTodoSynchronizer;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import com.Timo.Timo.domain.todo.repository.TodoRepository;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.global.exception.CustomException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleCalendarConnectionCommandService {

  private final GoogleCalendarConnectionRepository calendarConnectionRepository;
  private final GoogleTokenRevocationOutboxRepository calendarRevocationOutboxRepository;
  private final CalendarTodoSynchronizer calendarTodoSynchronizer;
  private final TodoRepository todoRepository;

  @Transactional
  public GoogleCalendarConnectResponse saveConnection(
      User user,
      GoogleTokenResponse tokenResponse,
      GoogleUserInfoResponse userInfo
  ) {
    GoogleCalendarConnection calendarConnection = GoogleCalendarConnection.builder()
        .user(user)
        .calendarEmail(userInfo.email())
        .accessToken(tokenResponse.accessToken())
        .refreshToken(tokenResponse.refreshToken())
        .tokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.expiresIn()))
        .build();

    try {
      calendarConnectionRepository.saveAndFlush(calendarConnection);
    } catch (DataIntegrityViolationException e) {
      throw new CustomException(CalendarErrorCode.CALENDAR_ALREADY_CONNECTED);
    }

    todoRepository.clearDisconnectedFromCalendar(user.getId(), CalendarSource.GOOGLE, LocalDateTime.now());

    return GoogleCalendarConnectResponse.builder()
        .calendarConnected(true)
        .calendarEmail(calendarConnection.getCalendarEmail())
        .connectedAt(resolveConnectedAt(calendarConnection, user))
        .build();
  }

  @Transactional
  public void deleteConnection(GoogleCalendarConnection calendarConnection, LocalDate today) {
    calendarTodoSynchronizer.handleDisconnect(calendarConnection.getUser().getId(), CalendarSource.GOOGLE, today);

    String tokenToRevoke = calendarConnection.getRefreshToken() != null
        ? calendarConnection.getRefreshToken()
        : calendarConnection.getAccessToken();
    calendarRevocationOutboxRepository.save(
        GoogleTokenRevocationOutbox.builder()
            .token(tokenToRevoke)
            .build()
    );

    calendarConnectionRepository.delete(calendarConnection);
  }

  private String resolveConnectedAt(GoogleCalendarConnection calendarConnection, User user) {
    ZoneId userZone = ZoneId.of(user.getZoneId());
    ZonedDateTime zoned = calendarConnection.getConnectedAt()
        .atZone(ZoneOffset.UTC)
        .withZoneSameInstant(userZone);
    return zoned.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }
}
