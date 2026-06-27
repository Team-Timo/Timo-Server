package com.Timo.Timo.global.exception;

import com.Timo.Timo.global.exception.code.BaseErrorCode;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

	private final BaseErrorCode errorCode;

	public CustomException(BaseErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
