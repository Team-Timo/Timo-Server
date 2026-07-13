package com.Timo.Timo.domain.calendar.exception;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CalendarSuccessCode implements BaseSuccessCode {

  CALENDAR_AUTHORIZE_URL_ISSUED(HttpStatus.OK, "구글 인증 URL이 발급되었습니다."),
  CALENDAR_CONNECTED(HttpStatus.CREATED, "구글 캘린더가 연동되었습니다."),
  CALENDAR_DISCONNECTED(HttpStatus.OK, "구글 캘린더 연동이 해제되었습니다.");

  private final HttpStatus httpStatus;
  private final String message;
}
