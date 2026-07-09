package com.Timo.Timo.domain.focus.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.Timo.Timo.domain.home.dto.response.HomeResponse.SubtaskResponse;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.TagResponse;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.TodoResponse;
import com.Timo.Timo.domain.todo.enums.Weekday;
import com.fasterxml.jackson.annotation.JsonProperty;

public record FocusTodoResponse(
		LocalDate date,
		Weekday dayOfWeek,
		boolean hasTodo,
		FocusTodoDetailResponse todo
) {
	public static FocusTodoResponse of(LocalDate date, TodoResponse todo) {
		return new FocusTodoResponse(
				date,
				Weekday.from(date.getDayOfWeek()),
				true,
				FocusTodoDetailResponse.from(todo)
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
			Long todoId,
			String icon,
			String title,
			boolean completed,
			Integer durationSeconds,
			String priority,
			TagResponse tag,
			@JsonProperty("isRepeated")
			boolean isRepeated,
			List<SubtaskResponse> subtasks
	) {
		public static FocusTodoDetailResponse from(TodoResponse todo) {
			return new FocusTodoDetailResponse(
					todo.todoId(),
					todo.icon(),
					todo.title(),
					todo.completed(),
					todo.durationSeconds(),
					todo.priority(),
					todo.tag(),
					todo.isRepeated(),
					todo.subtasks()
			);
		}
	}
}
