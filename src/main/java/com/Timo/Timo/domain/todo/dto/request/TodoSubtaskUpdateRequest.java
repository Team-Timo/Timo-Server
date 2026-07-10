package com.Timo.Timo.domain.todo.dto.request;

import com.Timo.Timo.domain.todo.validation.ValidSubtaskContent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TodoSubtaskUpdateRequest(
		Long subtaskId,

		@NotBlank
		@ValidSubtaskContent
		String content,

		@NotNull
		Boolean completed
) { }
