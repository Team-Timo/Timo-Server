package com.Timo.Timo.global.exception.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record ErrorDto(
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Schema(example = "2026-07-06 17:51:50", type = "string")
	LocalDateTime timestamp,
	int status,
	String errorCode,
	String message,
	String path,
	String traceId
) {}
