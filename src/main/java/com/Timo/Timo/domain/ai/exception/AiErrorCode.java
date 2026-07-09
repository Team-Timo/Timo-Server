package com.Timo.Timo.domain.ai.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiErrorCode implements BaseErrorCode {

	AI_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AI_429", "AI 추천 요청이 많습니다. 잠시 후 다시 시도해주세요.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}