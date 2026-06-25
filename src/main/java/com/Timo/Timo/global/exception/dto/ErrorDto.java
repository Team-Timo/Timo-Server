package com.Timo.Timo.global.exception.dto;

public record ErrorDto(
	String timestamp,
	int status,
	String errorCode,
	String message,
	String path
) {
}
