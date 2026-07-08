package com.Timo.Timo.domain.terms.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermsSuccessCode implements BaseSuccessCode {
	TERMS_RETRIEVED(HttpStatus.OK, "약관을 조회했습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}