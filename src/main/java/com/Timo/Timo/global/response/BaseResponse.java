package com.Timo.Timo.global.response;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;

@JsonPropertyOrder({"status", "message", "data"})
@Getter
public class BaseResponse<T> {

	private final int status;
	private final String message;
	private final T data;

	private BaseResponse(int status, String message, T data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}

	public static <T> BaseResponse<T> onSuccess(BaseSuccessCode successCode, T data) {
		return new BaseResponse<>(
			successCode.getHttpStatus().value(),
			successCode.getMessage(),
			data
		);
	}
}
