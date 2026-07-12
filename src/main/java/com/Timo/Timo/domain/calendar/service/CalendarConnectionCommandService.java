package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.dto.response.CalendarConnectResponse;
import com.Timo.Timo.domain.calendar.dto.response.GoogleTokenResponse;
import com.Timo.Timo.domain.calendar.dto.response.GoogleUserInfoResponse;
import com.Timo.Timo.domain.calendar.entity.CalendarConnection;
import com.Timo.Timo.domain.calendar.repository.CalendarConnectionRepository;
import com.Timo.Timo.domain.user.entity.User;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
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

    calendarConnectionRepository.save(calendarConnection);

    return CalendarConnectResponse.builder()
        .calendarConnected(true)
        .calendarEmail(calendarConnection.getCalendarEmail())
        .connectedAt(calendarConnection.getConnectedAt())
        .build();
  }

  @Transactional
  public void deleteConnection(CalendarConnection calendarConnection) {
    calendarConnectionRepository.delete(calendarConnection);
  }
}