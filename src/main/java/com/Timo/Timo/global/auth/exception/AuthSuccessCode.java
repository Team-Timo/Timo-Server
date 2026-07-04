package com.Timo.Timo.global.auth.exception;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

  LOGIN(HttpStatus.OK, "AUTH_200", "로그인에 성공했습니다."),
  REISSUE(HttpStatus.OK, "AUTH_201", "토큰 재발급에 성공했습니다."),
  LOGOUT(HttpStatus.OK, "AUTH_202", "로그아웃에 성공했습니다."),
  WITHDRAW(HttpStatus.OK, "AUTH_203", "회원 탈퇴에 성공했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
