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

import io.swagger.v3.oas.annotations.media.Schema;

public record HomeResponse(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		HomeFilter filter,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		LocalDate baseDate,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		List<DayResponse> days
) {

	public record DayResponse(
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			LocalDate date,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			Weekday dayOfWeek,
			@JsonProperty("isHoliday")
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			boolean isHoliday,
			@JsonProperty("isToday")
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			boolean isToday,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			int totalCount,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			int completedCount,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			boolean hasSubtask,
			@JsonProperty("isRepeated")
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			boolean isRepeated,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			TodoTimerStatus timerStatus,
			Integer sortOrder,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			List<SubtaskResponse> subtasks
	) {
	}

	public record TagResponse(
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			Long tagId,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			String name
	) {
		public static TagResponse from(Tag tag) {
			Objects.requireNonNull(tag, "tag must not be null");
			return new TagResponse(tag.getId(), tag.getName());
		}
	}

	public record SubtaskResponse(
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			Long subtaskId,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			String content,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			boolean completed
	) {
		public static SubtaskResponse from(Subtask subtask, boolean completed) {
			return new SubtaskResponse(
					subtask.getId(),
					subtask.getContent(),
					completed
			);
		}
	}
}
