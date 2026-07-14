package com.Timo.Timo.domain.terms.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermsErrorCode implements BaseErrorCode {
	TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS_404", "존재하지 않는 약관입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}