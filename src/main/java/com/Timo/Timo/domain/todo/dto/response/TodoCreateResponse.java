package com.Timo.Timo.domain.todo.dto.response;

import com.Timo.Timo.domain.todo.entity.Todo;

import io.swagger.v3.oas.annotations.media.Schema;

public record TodoCreateResponse(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Long todoId
) {

	public static TodoCreateResponse from(Todo todo) {
		return new TodoCreateResponse(todo.getId());
	}
}
