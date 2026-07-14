package com.Timo.Timo.domain.focus.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.Timo.Timo.domain.home.dto.response.HomeResponse.SubtaskResponse;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.TagResponse;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.TodoResponse;
import com.Timo.Timo.domain.todo.enums.Weekday;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public record FocusTodoResponse(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		LocalDate date,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Weekday dayOfWeek,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		boolean hasTodo,
		FocusTodoDetailResponse todo
) {
	public static FocusTodoResponse of(LocalDate date, TodoResponse todo, String memo) {
		return new FocusTodoResponse(
				date,
				Weekday.from(date.getDayOfWeek()),
				true,
				FocusTodoDetailResponse.from(todo, memo)
		);
	}

	public static FocusTodoResponse empty(LocalDate date) {
		return new FocusTodoResponse(
				date,
				Weekday.from(date.getDayOfWeek()),
				false,
				null
		);
	}

	public record FocusTodoDetailResponse(
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			Long todoId,
			String icon,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			String title,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			boolean completed,
			Integer durationSeconds,
			String priority,
			TagResponse tag,
			@JsonProperty("isRepeated")
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			boolean isRepeated,
			String memo,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			List<SubtaskResponse> subtasks
	) {
		public static FocusTodoDetailResponse from(TodoResponse todo, String memo) {
			return new FocusTodoDetailResponse(
					todo.todoId(),
					todo.icon(),
					todo.title(),
					todo.completed(),
					todo.durationSeconds(),
					todo.priority(),
					todo.tag(),
					todo.isRepeated(),
					memo,
					todo.subtasks()
			);
		}
	}
}
