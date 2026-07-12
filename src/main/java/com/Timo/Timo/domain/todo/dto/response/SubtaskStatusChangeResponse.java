package com.Timo.Timo.domain.todo.dto.response;

import com.Timo.Timo.domain.todo.entity.Subtask;

import io.swagger.v3.oas.annotations.media.Schema;

public record SubtaskStatusChangeResponse(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Long subtaskId,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Boolean completed
) {
	public static SubtaskStatusChangeResponse from(Subtask subtask) {
		return new SubtaskStatusChangeResponse(
				subtask.getId(),
				subtask.isCompleted()
		);
	}
}
