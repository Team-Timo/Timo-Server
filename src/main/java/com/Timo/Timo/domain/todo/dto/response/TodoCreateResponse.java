package com.Timo.Timo.domain.todo.dto.response;

import com.Timo.Timo.domain.todo.entity.Todo;

public record TodoCreateResponse(
		Long todoId
) {

	public static TodoCreateResponse from(Todo todo) {
		return new TodoCreateResponse(todo.getId());
	}
}
