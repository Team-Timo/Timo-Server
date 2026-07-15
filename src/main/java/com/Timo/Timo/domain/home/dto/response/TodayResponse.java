package com.Timo.Timo.domain.home.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.Timo.Timo.domain.home.dto.response.HomeResponse.SubtaskResponse;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.TagResponse;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.TodoResponse;
import com.Timo.Timo.domain.todo.enums.TodoTimerStatus;
import com.Timo.Timo.domain.todo.enums.Weekday;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public record TodayResponse(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		LocalDate date,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Weekday dayOfWeek,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		int totalCount,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		int completedCount,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		List<TodayTodoResponse> todos
) {

	public static TodayResponse of(LocalDate date, List<TodoResponse> todos) {
		List<TodayTodoResponse> todayTodos = todos.stream()
				.map(todo -> TodayTodoResponse.from(date, todo))
				.toList();

		int completedCount = (int) todayTodos.stream()
				.filter(TodayTodoResponse::completed)
				.count();

		return new TodayResponse(
				date,
				Weekday.from(date.getDayOfWeek()),
				todayTodos.size(),
				completedCount,
				todayTodos
		);
	}

	public record TodayTodoResponse(
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			Long todoId,
			String icon,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			String title,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			boolean completed,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			LocalDate date,
			Integer durationSeconds,
			String priority,
			TagResponse tag,
			@JsonProperty("isRepeated")
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			boolean isRepeated,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			boolean hasSubtask,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			TodoTimerStatus timerStatus,
			Integer sortOrder,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			List<SubtaskResponse> subtasks
	) {
		public static TodayTodoResponse from(LocalDate date, TodoResponse todo) {
			return new TodayTodoResponse(
					todo.todoId(),
					todo.icon(),
					todo.title(),
					todo.completed(),
					date,
					todo.durationSeconds(),
					todo.priority(),
					todo.tag(),
					todo.isRepeated(),
					todo.hasSubtask(),
					todo.timerStatus(),
					todo.sortOrder(),
					todo.subtasks()
			);
		}
	}
}
