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
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

  private final CalendarConnectionRepository calendarConnectionRepository;
  private final UserRepository userRepository;
  private final GoogleOAuthClient googleOAuthClient;
  private final CalendarConnectionCommandService commandService;

  public CalendarConnectResponse connect(Long userId, CalendarConnectRequest request) {
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

    String accessToken = calendarConnection.getAccessToken();

    deleteConnection(calendarConnection);

    try {
      googleOAuthClient.revokeToken(accessToken);
    } catch (Exception e) {
      log.warn("Google token revoke failed after disconnect. userId={}", userId, e);
    }

    return CalendarDisconnectResponse.builder()
        .calendarConnected(false)
        .build();
  }

  @Transactional
  public void deleteConnection(CalendarConnection calendarConnection) {
    calendarConnectionRepository.delete(calendarConnection);
  }
}
