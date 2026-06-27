package com.Timo.Timo.global.exception.code;

import org.springframework.http.HttpStatus;

public interface BaseSuccessCode {

	HttpStatus getHttpStatus();

	String getMessage();
}
