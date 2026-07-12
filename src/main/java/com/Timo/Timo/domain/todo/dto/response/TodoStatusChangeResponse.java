package com.Timo.Timo.domain.todo.dto.response;

import com.Timo.Timo.domain.todo.entity.TodoInstance;

import io.swagger.v3.oas.annotations.media.Schema;

public record TodoStatusChangeResponse(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Long todoId,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Boolean completed,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
