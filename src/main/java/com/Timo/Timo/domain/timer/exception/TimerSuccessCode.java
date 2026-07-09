package com.Timo.Timo.domain.timer.exception;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TimerSuccessCode implements BaseSuccessCode {

  TIMER_PAUSED(HttpStatus.OK, "TIMER_200", "타이머가 일시정지되었습니다."),
  TIMER_RESUMED(HttpStatus.OK, "TIMER_200", "타이머가 재개되었습니다."),
  TIMER_COMPLETED(HttpStatus.OK, "TIMER_200", "타이머가 완료되었습니다."),
  TIMER_STOPPED(HttpStatus.OK, "TIMER_200", "타이머가 종료되었습니다."),
  TIMER_STARTED(HttpStatus.CREATED, "TIMER_201", "타이머가 시작되었습니다."),
  ;

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
