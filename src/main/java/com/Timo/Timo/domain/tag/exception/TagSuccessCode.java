package com.Timo.Timo.domain.tag.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagSuccessCode implements BaseSuccessCode {

	CREATED(HttpStatus.CREATED, "태그가 생성되었습니다."),
	DELETED(HttpStatus.OK, "태그가 삭제되었습니다."),
	TAG_LIST_RETRIEVED(HttpStatus.OK, "태그 목록 조회 성공"),
	;

	private final HttpStatus httpStatus;
	private final String message;
}
