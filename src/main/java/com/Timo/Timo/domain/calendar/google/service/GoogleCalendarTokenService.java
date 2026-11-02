package com.Timo.Timo.domain.calendar.google.service;

import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.calendar.google.client.GoogleOAuthClient;
import com.Timo.Timo.domain.calendar.google.dto.client.GoogleTokenResponse;
import com.Timo.Timo.domain.calendar.google.entity.GoogleCalendarConnection;
import com.Timo.Timo.domain.calendar.google.repository.GoogleCalendarConnectionRepository;
import com.Timo.Timo.global.exception.CustomException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleCalendarTokenService {

  private final GoogleOAuthClient googleOAuthClient;
  private final GoogleCalendarConnectionRepository calendarConnectionRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public String ensureValidAccessToken(Long userId) {
    GoogleCalendarConnection connection = calendarConnectionRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(CalendarErrorCode.CALENDAR_NOT_CONNECTED));

    LocalDateTime expiresAt = connection.getTokenExpiresAt();
    boolean expiringSoon = expiresAt == null || expiresAt.isBefore(LocalDateTime.now().plusMinutes(2));

    if (!expiringSoon) {
      return connection.getAccessToken();
    }

    String refreshToken = connection.getRefreshToken();
    if (refreshToken == null) {
      throw new CustomException(CalendarErrorCode.CALENDAR_AUTH_FAILED);
    }

    GoogleTokenResponse refreshed = googleOAuthClient.refreshAccessToken(refreshToken);
    LocalDateTime newExpiresAt = LocalDateTime.now().plusSeconds(refreshed.expiresIn());
    connection.updateAccessToken(refreshed.accessToken(), newExpiresAt);
    return refreshed.accessToken();
  }
}
