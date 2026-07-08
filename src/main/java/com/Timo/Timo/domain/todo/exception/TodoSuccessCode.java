package com.Timo.Timo.domain.todo.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TodoSuccessCode implements BaseSuccessCode {

	CREATED(HttpStatus.CREATED, "TODO가 생성되었습니다."),
	STATUS_CHANGED(HttpStatus.OK, "TODO 상태가 변경되었습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
