package com.Timo.Timo.global.auth.exception;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

  LOGIN_SUCCESS(HttpStatus.OK, "AUTH_200", "로그인에 성공했습니다."),
  REISSUE_SUCCESS(HttpStatus.OK, "AUTH_200", "토큰 재발급에 성공했습니다."),
  LOGOUT_SUCCESS(HttpStatus.OK, "AUTH_200", "로그아웃에 성공했습니다."),
  WITHDRAW_SUCCESS(HttpStatus.OK, "AUTH_200", "회원 탈퇴에 성공했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
