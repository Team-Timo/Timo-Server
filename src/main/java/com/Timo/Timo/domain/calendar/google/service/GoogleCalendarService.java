package com.Timo.Timo.domain.calendar.google.service;

import com.Timo.Timo.domain.calendar.dto.response.CalendarDisconnectResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.calendar.google.client.GoogleOAuthClient;
import com.Timo.Timo.domain.calendar.google.dto.client.GoogleTokenResponse;
import com.Timo.Timo.domain.calendar.google.dto.client.GoogleUserInfoResponse;
import com.Timo.Timo.domain.calendar.google.dto.request.GoogleCalendarConnectRequest;
import com.Timo.Timo.domain.calendar.google.dto.response.GoogleCalendarConnectResponse;
import com.Timo.Timo.domain.calendar.google.entity.GoogleCalendarConnection;
import com.Timo.Timo.domain.calendar.google.repository.GoogleCalendarConnectionRepository;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;
import com.Timo.Timo.global.exception.code.ErrorCode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
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
public class GoogleCalendarService {

  private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
  private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar.readonly email";
  private static final String CALLBACK_PATH = "/oauth/calendar/callback";
  private static final Duration STATE_TTL = Duration.ofMinutes(5);

  private final GoogleCalendarConnectionRepository calendarConnectionRepository;
  private final UserRepository userRepository;
  private final GoogleOAuthClient googleOAuthClient;
  private final GoogleCalendarConnectionCommandService commandService;
  private final StringRedisTemplate redisTemplate;
  private final GoogleOAuthStateValidator calendarStateValidator;

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String clientId;

  @Value("${app.calendar.redirect-uri}")
  private String redirectUri;

  @Value("${app.calendar.allowed-redirect-origins}")
  private List<String> allowedRedirectOrigins;

  public String buildAuthorizationUrl(Long userId, String redirectOrigin) {
    String resolvedRedirectUri = resolveRedirectUri(redirectOrigin);
    String state = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set(
        GoogleOAuthStateValidator.STATE_KEY_PREFIX + state,
        userId + GoogleOAuthStateValidator.VALUE_DELIMITER + resolvedRedirectUri,
        STATE_TTL
    );

    return GOOGLE_AUTH_URL
        + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
        + "&redirect_uri=" + URLEncoder.encode(resolvedRedirectUri, StandardCharsets.UTF_8)
        + "&response_type=code"
        + "&scope=" + URLEncoder.encode(CALENDAR_SCOPE, StandardCharsets.UTF_8)
        + "&access_type=offline"
        + "&prompt=consent"
        + "&state=" + state;
  }

  private String resolveRedirectUri(String redirectOrigin) {
    if (redirectOrigin == null || redirectOrigin.isBlank()) {
      return redirectUri;
    }
    if (!allowedRedirectOrigins.contains(redirectOrigin)) {
      log.warn("허용되지 않은 redirectOrigin: {}", redirectOrigin);
      throw new CustomException(ErrorCode.BAD_REQUEST);
    }
    return redirectOrigin + CALLBACK_PATH;
  }

  public GoogleCalendarConnectResponse connect(Long userId, GoogleCalendarConnectRequest request) {
    String savedRedirectUri = calendarStateValidator.validateState(userId, request.state());
    String tokenRedirectUri = savedRedirectUri != null ? savedRedirectUri : redirectUri;

    if (calendarConnectionRepository.existsByUserId(userId)) {
      throw new CustomException(CalendarErrorCode.GOOGLE_CALENDAR_ALREADY_CONNECTED);
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    GoogleTokenResponse tokenResponse = googleOAuthClient.exchangeToken(request.authorizationCode(), tokenRedirectUri);
    GoogleUserInfoResponse userInfo = googleOAuthClient.fetchUserInfo(tokenResponse.accessToken());
    validateSameAccount(user, userInfo);

    return commandService.saveConnection(user, tokenResponse, userInfo);
  }

  private void validateSameAccount(User user, GoogleUserInfoResponse userInfo) {
    if (!user.getEmail().equalsIgnoreCase(userInfo.email())) {
      throw new CustomException(CalendarErrorCode.GOOGLE_CALENDAR_EMAIL_MISMATCH);
    }
  }

  public CalendarDisconnectResponse disconnect(Long userId) {
    GoogleCalendarConnection calendarConnection = calendarConnectionRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(CalendarErrorCode.GOOGLE_CALENDAR_NOT_CONNECTED));

    ZoneId userZone = ZoneId.of(calendarConnection.getUser().getZoneId());
    commandService.deleteConnection(calendarConnection, LocalDate.now(userZone));

    return CalendarDisconnectResponse.builder()
        .calendarConnected(false)
        .build();
  }

  @Transactional
  public void deleteConnection(GoogleCalendarConnection calendarConnection) {
    calendarConnectionRepository.delete(calendarConnection);
  }
}
