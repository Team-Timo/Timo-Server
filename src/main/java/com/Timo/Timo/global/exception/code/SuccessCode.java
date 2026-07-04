package com.Timo.Timo.global.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements BaseSuccessCode {

	OK(HttpStatus.OK, "COMMON_200", "요청에 성공했습니다."),
	CREATED(HttpStatus.CREATED, "COMMON_201", "요청에 성공하여 리소스가 생성되었습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
