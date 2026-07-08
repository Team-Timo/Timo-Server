package com.Timo.Timo.domain.todo.dto.response;

import com.Timo.Timo.domain.todo.entity.TodoInstance;

public record TodoStatusChangeResponse(
		Long todoId,
		Boolean completed,
		Integer sortOrder
) {

	public static TodoStatusChangeResponse from(Long todoId, TodoInstance instance) {
		return new TodoStatusChangeResponse(
				todoId,
				instance.isCompleted(),
				instance.getSortOrder()
		);
	}
}
