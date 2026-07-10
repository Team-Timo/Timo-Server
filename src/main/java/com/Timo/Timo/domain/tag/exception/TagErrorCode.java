package com.Timo.Timo.domain.tag.exception;

import org.springframework.http.HttpStatus;

import com.Timo.Timo.global.exception.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagErrorCode implements BaseErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "TAG_400", "태그 이름은 필수입니다."),
	INVALID_TAG_ID(HttpStatus.BAD_REQUEST, "TAG_400", "유효하지 않은 태그 ID입니다."),
	TAG_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "TAG_403", "삭제할 수 없는 태그입니다."),
	TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "TAG_404", "존재하지 않는 태그입니다."),
	DUPLICATE_TAG_NAME(HttpStatus.CONFLICT, "TAG_409", "이미 존재하는 태그명입니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
