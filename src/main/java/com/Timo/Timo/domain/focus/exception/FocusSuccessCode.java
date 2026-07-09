package com.Timo.Timo.domain.focus.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FocusSuccessCode implements BaseSuccessCode {

	GET_FOCUS_TODO(HttpStatus.OK, "집중 모드 TODO 조회 성공"),
	NO_TODO_TODAY(HttpStatus.OK, "오늘의 TODO가 없습니다."),
	ALL_TODO_COMPLETED(HttpStatus.OK, "오늘의 TODO를 모두 완료했습니다."),
	;

	private final HttpStatus httpStatus;
	private final String message;
}
