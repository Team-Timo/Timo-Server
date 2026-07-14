package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.client.GoogleOAuthClient;
import com.Timo.Timo.domain.calendar.dto.client.GoogleTokenResponse;
import com.Timo.Timo.domain.calendar.entity.CalendarConnection;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalendarTokenService {

  private final GoogleOAuthClient googleOAuthClient;

  @Transactional
  public String ensureValidAccessToken(CalendarConnection connection) {
    LocalDateTime expiresAt = connection.getTokenExpiresAt();
    boolean expiringSoon = expiresAt == null || expiresAt.isBefore(LocalDateTime.now().plusMinutes(2));

    if (!expiringSoon) {
      return connection.getAccessToken();
    }

    GoogleTokenResponse refreshed = googleOAuthClient.refreshAccessToken(connection.getRefreshToken());
    LocalDateTime newExpiresAt = LocalDateTime.now().plusSeconds(refreshed.expiresIn());
    connection.updateAccessToken(refreshed.accessToken(), newExpiresAt);
    return refreshed.accessToken();
  }
}
