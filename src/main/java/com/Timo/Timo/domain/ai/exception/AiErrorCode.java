package com.Timo.Timo.domain.ai.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiErrorCode implements BaseErrorCode {

	AI_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AI_429", "AI 추천 요청이 많습니다. 잠시 후 다시 시도해주세요."),
	AI_CONFIG_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "AI_500", "AI 설정 정보가 올바르지 않습니다."),
	AI_RESPONSE_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI_500", "AI 응답을 처리하는 중 오류가 발생했습니다."),
	AI_INVALID_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "AI_500", "AI 응답 형식이 올바르지 않습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}