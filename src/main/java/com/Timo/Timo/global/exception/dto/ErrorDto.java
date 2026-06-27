package com.Timo.Timo.global.exception.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ErrorDto(
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime timestamp,
	int status,
	String errorCode,
	String message,
	String path
) {
}
