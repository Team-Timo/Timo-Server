package com.Timo.Timo.domain.ai.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiSuccessCode implements BaseSuccessCode {

	DURATION_RECOMMENDED(HttpStatus.OK, "AI 예상 소요 시간을 추천했습니다."),
	TODO_FEEDBACK_CREATED(HttpStatus.OK, "AI 투두 피드백을 생성했습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}