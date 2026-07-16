package com.Timo.Timo.domain.todo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record TodoMemoUpdateRequest(
		@Size(max = 300)
		@Schema(description = "해당 날짜에 저장할 메모 (null이면 규칙 메모로 폴백)", example = "오늘은 30분만")
		String memo
) { }
