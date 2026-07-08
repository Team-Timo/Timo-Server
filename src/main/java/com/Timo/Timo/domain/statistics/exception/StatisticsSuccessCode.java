package com.Timo.Timo.domain.statistics.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatisticsSuccessCode implements BaseSuccessCode {

	CALENDAR_RETRIEVED(HttpStatus.OK, "통계 캘린더를 조회했습니다."),
	SUMMARY_RETRIEVED(HttpStatus.OK, "월별 통계 요약을 조회했습니다."),
	DAILY_RETRIEVED(HttpStatus.OK, "일별 기록 조회에 성공했습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
