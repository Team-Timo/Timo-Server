package com.Timo.Timo.domain.home.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HomeErrorCode implements BaseErrorCode {

	INVALID_FILTER_OR_DATE(HttpStatus.BAD_REQUEST, "HOME_400", "유효하지 않은 필터 값이거나 날짜 형식이 올바르지 않습니다."),
	INVALID_DATE(HttpStatus.BAD_REQUEST, "DATE_400", "날짜 형식이 올바르지 않습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
