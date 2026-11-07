package com.Timo.Timo.domain.calendar.exception;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CalendarSuccessCode implements BaseSuccessCode {

  GOOGLE_CALENDAR_AUTHORIZE_URL_ISSUED(HttpStatus.OK, "구글 인증 URL이 발급되었습니다."),
  GOOGLE_CALENDAR_CONNECTED(HttpStatus.CREATED, "구글 캘린더가 연동되었습니다."),
  GOOGLE_CALENDAR_DISCONNECTED(HttpStatus.OK, "구글 캘린더 연동이 해제되었습니다."),

  APPLE_CALENDAR_CONNECTED(HttpStatus.CREATED, "애플 캘린더가 연동되었습니다."),
  APPLE_CALENDAR_DISCONNECTED(HttpStatus.OK, "애플 캘린더 연동이 해제되었습니다. account.apple.com에서 앱 전용 암호도 삭제해주세요."),

  CALENDAR_EVENTS_RETRIEVED(HttpStatus.OK, "캘린더 일정 조회 성공"),
  CALENDAR_EVENTS_SYNCED(HttpStatus.OK, "캘린더 일정 동기화 성공"),
  ;

  private final HttpStatus httpStatus;
  private final String message;
}
