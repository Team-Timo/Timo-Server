package com.Timo.Timo.domain.todo.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TodoErrorCode implements BaseErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "TODO_400", "필수 필드가 누락되었거나 형식이 올바르지 않습니다."),
	INVALID_TITLE(HttpStatus.BAD_REQUEST, "TODO_400", "투두명은 한국어 20자/영어 30자를 초과할 수 없습니다."),
	NO_UPDATE_FIELDS(HttpStatus.BAD_REQUEST, "COMMON_400", "수정할 필드가 없습니다."),
  	TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "TODO_404", "존재하지 않는 투두입니다"),
  	MAX_COUNT_EXCEEDED(HttpStatus.CONFLICT, "TODO_409", "해당 날짜의 투두가 최대 개수(20개)를 초과했습니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
