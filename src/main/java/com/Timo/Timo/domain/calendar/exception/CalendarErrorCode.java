package com.Timo.Timo.domain.calendar.exception;

import com.Timo.Timo.global.exception.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CalendarErrorCode implements BaseErrorCode {

  CALENDAR_401_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "CALENDAR_401", "구글 캘린더 인증에 실패했습니다."),
  CALENDAR_401_EMAIL_MISMATCH(HttpStatus.UNAUTHORIZED, "CALENDAR_401", "가입 시 사용한 구글 계정으로만 연동할 수 있습니다."),
  CALENDAR_404_NOT_CONNECTED(HttpStatus.NOT_FOUND, "CALENDAR_404", "연동된 캘린더가 없습니다."),
  CALENDAR_409_ALREADY_CONNECTED(HttpStatus.CONFLICT, "CALENDAR_409", "이미 캘린더가 연동되어 있습니다."),
  CALENDAR_500_REVOKE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CALENDAR_500", "구글 토큰 해제에 실패했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
