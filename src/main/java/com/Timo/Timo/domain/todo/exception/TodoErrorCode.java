package com.Timo.Timo.domain.todo.exception;

import com.Timo.Timo.global.exception.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TodoErrorCode implements BaseErrorCode {

  TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "TODO_404", "존재하지 않는 투두입니다."),
  TODO_ESTIMATED_MINUTES_REQUIRED(HttpStatus.BAD_REQUEST, "TODO_400", "예상 소요 시간이 설정되지 않은 투두입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
