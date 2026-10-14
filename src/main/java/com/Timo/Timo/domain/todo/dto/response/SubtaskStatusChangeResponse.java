package com.Timo.Timo.domain.todo.dto.response;

import com.Timo.Timo.domain.todo.entity.SubtaskCompletion;

import io.swagger.v3.oas.annotations.media.Schema;

public record SubtaskStatusChangeResponse(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Long subtaskId,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Boolean completed,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Integer sortOrder
) {

	public static SubtaskStatusChangeResponse from(SubtaskCompletion completion) {
		return new SubtaskStatusChangeResponse(
				completion.getSubtask().getId(),
				completion.isCompleted(),
				completion.getSortOrder()
		);
	}
}
