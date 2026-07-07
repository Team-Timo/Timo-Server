package com.Timo.Timo.domain.user.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserSuccessCode implements BaseSuccessCode {

	PROFILE_RETRIEVED(HttpStatus.OK, "프로필을 조회했습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
