package com.Timo.Timo.domain.timer.exception;

import com.Timo.Timo.global.exception.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TimerErrorCode implements BaseErrorCode {

  TIMER_NOT_FOUND(HttpStatus.NOT_FOUND, "TIMER_404", "존재하지 않는 타이머입니다."),
  TIMER_ALREADY_RUNNING(HttpStatus.CONFLICT, "TIMER_409", "이미 실행 중인 타이머가 있습니다."),
  TIMER_INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "TIMER_409", "요청을 처리할 수 없는 타이머 상태입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
