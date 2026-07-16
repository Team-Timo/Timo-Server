package com.Timo.Timo.domain.todo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record SubtaskStatusChangeResponse(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Long subtaskId,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Boolean completed
) {
}
