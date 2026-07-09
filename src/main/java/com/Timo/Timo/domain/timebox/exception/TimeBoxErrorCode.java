package com.Timo.Timo.domain.timebox.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeBoxErrorCode implements BaseErrorCode {

	DATE_REQUIRED(HttpStatus.BAD_REQUEST, "COMMON_400", "date는 필수입니다."),
	INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "COMMON_400", "date는 yyyy-MM-dd 형식이어야 합니다."),
	INVALID_DATE(HttpStatus.BAD_REQUEST, "COMMON_400", "유효하지 않은 날짜입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}