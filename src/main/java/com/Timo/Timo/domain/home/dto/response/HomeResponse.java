package com.Timo.Timo.domain.home.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.Timo.Timo.domain.home.enums.HomeFilter;
import com.Timo.Timo.domain.tag.entity.Tag;
import com.Timo.Timo.domain.todo.entity.Subtask;
import com.Timo.Timo.domain.todo.enums.TodoTimerStatus;
import com.Timo.Timo.domain.todo.enums.Weekday;
import com.fasterxml.jackson.annotation.JsonProperty;

public record HomeResponse(
		HomeFilter filter,
		LocalDate baseDate,
		List<DayResponse> days
) {

	public record DayResponse(
			LocalDate date,
			Weekday dayOfWeek,
			@JsonProperty("isHoliday")
			boolean isHoliday,
			@JsonProperty("isToday")
			boolean isToday,
			int totalCount,
			int completedCount,
			List<TodoResponse> todos
	) {
		public static DayResponse of(
				LocalDate date,
				boolean isHoliday,
				boolean isToday,
				List<TodoResponse> todos
		) {
			int completedCount = (int) todos.stream()
					.filter(TodoResponse::completed)
					.count();

			return new DayResponse(
					date,
					Weekday.from(date.getDayOfWeek()),
					isHoliday,
					isToday,
					todos.size(),
					completedCount,
					todos
			);
		}
	}

	public record TodoResponse(
			Long todoId,
			String icon,
			String title,
			boolean completed,
			Integer durationSeconds,
			String priority,
			TagResponse tag,
			boolean hasMemo,
			@JsonProperty("isRepeated")
			boolean isRepeated,
			TodoTimerStatus timerStatus,
			Integer sortOrder,
			List<SubtaskResponse> subtasks
	) {
	}

	public record TagResponse(
			Long tagId,
			String name
	) {
		public static TagResponse from(Tag tag) {
			Objects.requireNonNull(tag, "tag must not be null");
			return new TagResponse(tag.getId(), tag.getName());
		}
	}

	public record SubtaskResponse(
			Long subtaskId,
			String content,
			boolean completed
	) {
		public static SubtaskResponse from(Subtask subtask) {
			return new SubtaskResponse(
					subtask.getId(),
					subtask.getContent(),
					subtask.isCompleted()
			);
		}
	}
}
