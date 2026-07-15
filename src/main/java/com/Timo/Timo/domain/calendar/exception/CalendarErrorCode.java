package com.Timo.Timo.domain.calendar.exception;

import com.Timo.Timo.global.exception.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CalendarErrorCode implements BaseErrorCode {

  INVALID_FILTER_OR_DATE(HttpStatus.BAD_REQUEST, "CALENDAR_400", "유효하지 않은 필터 값이거나 날짜 형식이 올바르지 않습니다."),
  CALENDAR_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "CALENDAR_401", "구글 캘린더 인증에 실패했습니다."),
  CALENDAR_EMAIL_MISMATCH(HttpStatus.UNAUTHORIZED, "CALENDAR_401", "가입 시 사용한 구글 계정으로만 연동할 수 있습니다."),
  CALENDAR_STATE_MISMATCH(HttpStatus.UNAUTHORIZED, "CALENDAR_401", "유효하지 않은 인증 요청입니다."),
  CALENDAR_NOT_CONNECTED(HttpStatus.NOT_FOUND, "CALENDAR_404", "연동된 캘린더가 없습니다."),
  CALENDAR_ALREADY_CONNECTED(HttpStatus.CONFLICT, "CALENDAR_409", "이미 캘린더가 연동되어 있습니다."),
  CALENDAR_REVOKE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CALENDAR_500", "구글 토큰 해제에 실패했습니다."),
  CALENDAR_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "CALENDAR_429", "구글 캘린더 요청이 일시적으로 제한되었습니다. 잠시 후 다시 시도해주세요."),
  CALENDAR_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CALENDAR_500", "캘린더 처리 중 서버 오류가 발생했습니다."),
  CALENDAR_UPSTREAM_ERROR(HttpStatus.BAD_GATEWAY, "CALENDAR_502", "구글 캘린더 서버와 통신 중 오류가 발생했습니다."),
  CALENDAR_TIMEOUT(HttpStatus.SERVICE_UNAVAILABLE, "CALENDAR_503", "구글 서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."),
  ;
  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
