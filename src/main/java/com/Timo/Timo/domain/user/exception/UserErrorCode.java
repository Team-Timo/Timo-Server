package com.Timo.Timo.domain.user.exception;

import com.Timo.Timo.global.exception.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

  USER_INVALID_TIME(HttpStatus.BAD_REQUEST, "USER_400", "취침 시간은 기상 시간보다 이후여야 합니다."),
  INVALID_TIMEZONE(HttpStatus.BAD_REQUEST, "USER_400", "유효하지 않은 시간대 ID입니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "존재하지 않는 사용자입니다."),
  ;

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
