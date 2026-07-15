package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.dto.response.CalendarConnectResponse;
import com.Timo.Timo.domain.calendar.dto.client.GoogleTokenResponse;
import com.Timo.Timo.domain.calendar.dto.client.GoogleUserInfoResponse;
import com.Timo.Timo.domain.calendar.entity.CalendarConnection;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.calendar.repository.CalendarConnectionRepository;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.global.exception.CustomException;
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
public class CalendarConnectionCommandService {

  private final CalendarConnectionRepository calendarConnectionRepository;

  @Transactional
  public CalendarConnectResponse saveConnection(
      User user,
      GoogleTokenResponse tokenResponse,
      GoogleUserInfoResponse userInfo
  ) {
    CalendarConnection calendarConnection = CalendarConnection.builder()
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

    return CalendarConnectResponse.builder()
        .calendarConnected(true)
        .calendarEmail(calendarConnection.getCalendarEmail())
        .connectedAt(resolveConnectedAt(calendarConnection, user))
        .build();
  }

  @Transactional
  public void deleteConnection(CalendarConnection calendarConnection) {
    calendarConnectionRepository.delete(calendarConnection);
  }

  private String resolveConnectedAt(CalendarConnection calendarConnection, User user) {
    ZoneId userZone = ZoneId.of(user.getZoneId());
    ZonedDateTime zoned = calendarConnection.getConnectedAt()
        .atZone(ZoneOffset.UTC)
        .withZoneSameInstant(userZone);
    return zoned.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }
}
