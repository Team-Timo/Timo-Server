package com.Timo.Timo.global.auth.exception;

import com.Timo.Timo.global.exception.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

  OAUTH2_INVALID_USER_INFO(HttpStatus.UNAUTHORIZED, "AUTH_401", "OAuth2 유저 정보가 유효하지 않습니다."),
  OAUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_402", "소셜 로그인에 실패했습니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_403", "유효하지 않거나 만료된 리프레시 토큰입니다."),
  INVALID_AUTH_CODE(HttpStatus.UNAUTHORIZED, "AUTH_404", "유효하지 않거나 만료된 인증 코드입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
