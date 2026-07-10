package com.Timo.Timo.domain.todo.dto.response;

import com.Timo.Timo.domain.todo.entity.Subtask;

public record SubtaskStatusChangeResponse(
		Long subtaskId,
		Boolean completed
) {
	public static SubtaskStatusChangeResponse from(Subtask subtask) {
		return new SubtaskStatusChangeResponse(
				subtask.getId(),
				subtask.isCompleted()
		);
	}
}
