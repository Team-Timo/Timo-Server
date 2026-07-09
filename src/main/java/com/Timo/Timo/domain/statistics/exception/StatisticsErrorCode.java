package com.Timo.Timo.domain.statistics.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatisticsErrorCode implements BaseErrorCode {

	YEAR_MONTH_REQUIRED(HttpStatus.BAD_REQUEST, "STATISTICS_400", "yearMonth는 필수입니다."),
	INVALID_YEAR_MONTH_FORMAT(HttpStatus.BAD_REQUEST, "STATISTICS_400", "yearMonth는 yyyy-MM 형식이어야 합니다."),
	INVALID_YEAR_MONTH(HttpStatus.BAD_REQUEST, "STATISTICS_400", "유효하지 않은 연월입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}