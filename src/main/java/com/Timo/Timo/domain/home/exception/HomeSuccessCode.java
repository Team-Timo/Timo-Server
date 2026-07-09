package com.Timo.Timo.domain.home.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HomeSuccessCode implements BaseSuccessCode {

	GET_HOME(HttpStatus.OK, "홈 뷰 조회 성공"),
	GET_TODAY(HttpStatus.OK, "오늘 뷰 조회 성공");

	private final HttpStatus httpStatus;
	private final String message;
}
