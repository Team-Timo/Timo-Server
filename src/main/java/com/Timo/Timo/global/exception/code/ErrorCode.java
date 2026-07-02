package com.Timo.Timo.global.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseErrorCode {

	BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "접근 권한이 없습니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "요청한 리소스를 찾을 수 없습니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405", "지원하지 않는 HTTP 메서드입니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다."),

  // Auth
  OAUTH2_INVALID_USER_INFO(HttpStatus.UNAUTHORIZED, "AUTH_401", "OAuth2 유저 정보가 유효하지 않습니다."),
  OAUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_402", "소셜 로그인에 실패했습니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_403", "유효하지 않거나 만료된 리프레시 토큰입니다."),
  INVALID_AUTH_CODE(HttpStatus.UNAUTHORIZED, "AUTH_404", "유효하지 않거나 만료된 인증 코드입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
