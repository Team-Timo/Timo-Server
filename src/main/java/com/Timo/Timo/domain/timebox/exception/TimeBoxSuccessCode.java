package com.Timo.Timo.domain.timebox.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeBoxSuccessCode implements BaseSuccessCode {

	TIME_BOXES_RETRIEVED(HttpStatus.OK, "타임박스를 조회했습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}