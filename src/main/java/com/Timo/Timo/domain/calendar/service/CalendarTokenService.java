package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.client.GoogleOAuthClient;
import com.Timo.Timo.domain.calendar.dto.client.GoogleTokenResponse;
import com.Timo.Timo.domain.calendar.entity.CalendarConnection;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.calendar.repository.CalendarConnectionRepository;
import com.Timo.Timo.global.exception.CustomException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalendarTokenService {

  private final GoogleOAuthClient googleOAuthClient;
  private final CalendarConnectionRepository calendarConnectionRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public String ensureValidAccessToken(Long userId) {
    CalendarConnection connection = calendarConnectionRepository.findByUserId(userId)
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
