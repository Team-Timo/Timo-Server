package com.Timo.Timo.global.auth.controller;

import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.auth.dto.response.AuthTokenResponse;
import com.Timo.Timo.global.auth.exception.AuthErrorCode;
import com.Timo.Timo.global.auth.exception.AuthSuccessCode;
import com.Timo.Timo.global.auth.handler.AuthErrorResponseWriter;
import com.Timo.Timo.global.auth.service.AuthCodeService;
import com.Timo.Timo.global.exception.code.ErrorCode;
import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import com.Timo.Timo.global.response.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthCodeService authCodeService;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthErrorResponseWriter authErrorResponseWriter;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  @Operation(summary = "AccessToken 발급", description = "1회성 code로 AccessToken을 발급합니다.")
  @PostMapping("/token")
  public void token(
      @RequestBody Map<String, String> body,
      HttpServletRequest request,
      HttpServletResponse response
  ) throws IOException {

    String code = body.get("code");

    if (code == null) {
      authErrorResponseWriter.write(response, ErrorCode.BAD_REQUEST, request.getRequestURI());
      return;
    }

    String value = authCodeService.getAndDelete(code);

    if (value == null) {
      authErrorResponseWriter.write(response, AuthErrorCode.INVALID_AUTH_CODE, request.getRequestURI());
      return;
    }

    String[] parts = value.split(":");
    Long userId = Long.parseLong(parts[0]);
    boolean onboardingCompleted = Boolean.parseBoolean(parts[1]);
    boolean isNewUser = !onboardingCompleted;

    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      authErrorResponseWriter.write(response, ErrorCode.NOT_FOUND, request.getRequestURI());
      return;
    }

    String accessToken = jwtTokenProvider.generateAccessToken(userId);

    AuthTokenResponse authTokenResponse = AuthTokenResponse.builder()
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

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Cache-Control", "no-store");
    response.getWriter().write(
        objectMapper.writeValueAsString(
            BaseResponse.onSuccess(AuthSuccessCode.LOGIN, authTokenResponse)
        )
    );
  }
}