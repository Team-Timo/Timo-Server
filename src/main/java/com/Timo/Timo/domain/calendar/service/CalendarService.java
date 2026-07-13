package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.client.GoogleOAuthClient;
import com.Timo.Timo.domain.calendar.dto.request.CalendarConnectRequest;
import com.Timo.Timo.domain.calendar.dto.response.CalendarConnectResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarDisconnectResponse;
import com.Timo.Timo.domain.calendar.dto.response.GoogleTokenResponse;
import com.Timo.Timo.domain.calendar.dto.response.GoogleUserInfoResponse;
import com.Timo.Timo.domain.calendar.entity.CalendarConnection;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.calendar.repository.CalendarConnectionRepository;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

  private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
  private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar.readonly email";
  private static final Duration STATE_TTL = Duration.ofMinutes(5);

  private final CalendarConnectionRepository calendarConnectionRepository;
  private final UserRepository userRepository;
  private final GoogleOAuthClient googleOAuthClient;
  private final CalendarConnectionCommandService commandService;
  private final StringRedisTemplate redisTemplate;

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String clientId;

  @Value("${app.calendar.redirect-uri}")
  private String redirectUri;

  public String buildAuthorizationUrl(Long userId) {
    String state = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set("calendar:oauth:state:" + state, String.valueOf(userId), STATE_TTL);

    return GOOGLE_AUTH_URL
        + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
        + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
        + "&response_type=code"
        + "&scope=" + URLEncoder.encode(CALENDAR_SCOPE, StandardCharsets.UTF_8)
        + "&access_type=offline"
        + "&prompt=consent"
        + "&state=" + state;
  }

  public void validateState(Long userId, String state) {
    String key = "calendar:oauth:state:" + state;
    String savedUserId = redisTemplate.opsForValue().get(key);

    if (savedUserId == null || !savedUserId.equals(String.valueOf(userId))) {
      throw new CustomException(CalendarErrorCode.CALENDAR_STATE_MISMATCH);
    }
    redisTemplate.delete(key);
  }

  public CalendarConnectResponse connect(Long userId, CalendarConnectRequest request) {
    validateState(userId, request.state());

    if (calendarConnectionRepository.existsByUserId(userId)) {
      throw new CustomException(CalendarErrorCode.CALENDAR_ALREADY_CONNECTED);
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    GoogleTokenResponse tokenResponse = googleOAuthClient.exchangeToken(request.authorizationCode());
    GoogleUserInfoResponse userInfo = googleOAuthClient.fetchUserInfo(tokenResponse.accessToken());
    validateSameAccount(user, userInfo);

    return commandService.saveConnection(user, tokenResponse, userInfo);
  }

  private void validateSameAccount(User user, GoogleUserInfoResponse userInfo) {
    if (!user.getEmail().equalsIgnoreCase(userInfo.email())) {
      throw new CustomException(CalendarErrorCode.CALENDAR_EMAIL_MISMATCH);
    }
  }

  public CalendarDisconnectResponse disconnect(Long userId) {
    CalendarConnection calendarConnection = calendarConnectionRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(CalendarErrorCode.CALENDAR_NOT_CONNECTED));

    String tokenToRevoke = calendarConnection.getRefreshToken() != null
        ? calendarConnection.getRefreshToken()
        : calendarConnection.getAccessToken();

    googleOAuthClient.revokeToken(tokenToRevoke);

    commandService.deleteConnection(calendarConnection);

    return CalendarDisconnectResponse.builder()
        .calendarConnected(false)
        .build();
  }

  @Transactional
  public void deleteConnection(CalendarConnection calendarConnection) {
    calendarConnectionRepository.delete(calendarConnection);
  }
}
