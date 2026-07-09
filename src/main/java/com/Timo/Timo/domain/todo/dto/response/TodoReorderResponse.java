package com.Timo.Timo.domain.todo.dto.response;

import com.Timo.Timo.domain.todo.entity.TodoInstance;

public record TodoReorderResponse(
		Long todoId,
		Integer sortOrder
) {

	public static TodoReorderResponse from(Long todoId, TodoInstance instance) {
		return new TodoReorderResponse(
				todoId,
				instance.getSortOrder()
		);
	}
}
