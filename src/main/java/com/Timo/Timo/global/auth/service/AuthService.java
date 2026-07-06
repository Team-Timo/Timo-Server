package com.Timo.Timo.global.auth.service;

import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.auth.dto.response.AuthTokenResponse;
import com.Timo.Timo.global.auth.exception.AuthErrorCode;
import com.Timo.Timo.global.exception.CustomException;
import com.Timo.Timo.global.exception.code.ErrorCode;
import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthCodeService authCodeService;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  public AuthTokenResponse exchangeCodeForToken(String code) {
    if (code == null) {
      throw new CustomException(ErrorCode.BAD_REQUEST);
    }

    String value = authCodeService.getAndDelete(code);
    if (value == null) {
      throw new CustomException(AuthErrorCode.INVALID_AUTH_CODE);
    }

    String[] parts = value.split(":");
    Long userId = Long.parseLong(parts[0]);
    boolean onboardingCompleted = Boolean.parseBoolean(parts[1]);
    boolean isNewUser = !onboardingCompleted;

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    String accessToken = jwtTokenProvider.generateAccessToken(userId);

    return AuthTokenResponse.builder()
        .accessToken(accessToken)
        .isNewUser(isNewUser)
        .user(AuthTokenResponse.UserInfo.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .profileImageUrl(user.getProfileImageUrl())
            .onboardingCompleted(user.isOnboardingCompleted())
            .build()
        )
        .build();
  }
}
