package com.Timo.Timo.domain.todo.dto.response;

import com.Timo.Timo.domain.todo.entity.SubtaskCompletion;

import io.swagger.v3.oas.annotations.media.Schema;

public record SubtaskReorderResponse(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Long subtaskId,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Integer sortOrder
) {

	public static SubtaskReorderResponse from(SubtaskCompletion completion) {
		return new SubtaskReorderResponse(
				completion.getSubtask().getId(),
				completion.getSortOrder()
		);
	}
}
