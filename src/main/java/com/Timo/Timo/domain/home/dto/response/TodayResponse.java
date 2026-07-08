package com.Timo.Timo.domain.home.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.Timo.Timo.domain.home.dto.response.HomeResponse.SubtaskResponse;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.TagResponse;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.TodoResponse;
import com.Timo.Timo.domain.todo.enums.TodoTimerStatus;
import com.Timo.Timo.domain.todo.enums.Weekday;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TodayResponse(
		LocalDate date,
		Weekday dayOfWeek,
		int totalCount,
		int completedCount,
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
			Long todoId,
			String icon,
			String title,
			boolean completed,
			LocalDate date,
			Integer durationSeconds,
			String priority,
			TagResponse tag,
			@JsonProperty("isRepeated")
			boolean isRepeated,
			boolean hasMemo,
			TodoTimerStatus timerStatus,
			Integer sortOrder,
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
					todo.hasMemo(),
					todo.timerStatus(),
					todo.sortOrder(),
					todo.subtasks()
			);
		}
	}
}
