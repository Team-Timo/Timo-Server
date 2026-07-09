package com.Timo.Timo.domain.tag.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagSuccessCode implements BaseSuccessCode {

	CREATED(HttpStatus.CREATED, "태그가 생성되었습니다."),
	;

	private final HttpStatus httpStatus;
	private final String message;
}
