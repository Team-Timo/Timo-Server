package com.Timo.Timo.domain.todo.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.Timo.Timo.domain.tag.entity.Tag;
import com.Timo.Timo.domain.todo.entity.Subtask;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.entity.TodoInstance;
import com.Timo.Timo.domain.todo.enums.RepeatType;
import com.Timo.Timo.domain.todo.enums.TodoTimerStatus;
import com.Timo.Timo.domain.todo.enums.Weekday;

import io.swagger.v3.oas.annotations.media.Schema;

public record TodoDetailResponse(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Long todoId,
		String icon,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		String title,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		boolean completed,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		LocalDate date,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		String dayOfWeek,
		Integer durationSeconds,
		String priority,
		TagResponse tag,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		RepeatResponse repeat,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		TodoTimerStatus timerStatus,
		String memo,
		Integer sortOrder,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		List<SubtaskResponse> subtasks
) {

	public static TodoDetailResponse of(Todo todo, TodoInstance instance, LocalDate date, Tag tag,
			Set<Long> completedSubtaskIds) {
		return new TodoDetailResponse(
				todo.getId(),
				todo.getIcon() != null ? todo.getIcon().name() : null,
				todo.getTitle(),
				instance != null && instance.isCompleted(),
				date,
				Weekday.from(date.getDayOfWeek()).name(),
				todo.getDurationSeconds(),
				todo.getPriority() != null ? todo.getPriority().name() : null,
				TagResponse.from(tag),
				RepeatResponse.from(todo),
				instance != null ? instance.getTimerStatus() : TodoTimerStatus.STOPPED,
				instance != null ? instance.resolveMemo() : todo.getMemo(),
				instance != null ? instance.getSortOrder() : null,
				todo.getSubtasks().stream()
						.map(subtask -> SubtaskResponse.from(subtask, completedSubtaskIds.contains(subtask.getId())))
						.toList()
		);
	}

	public record TagResponse(
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			Long tagId,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			String name
	) {
		public static TagResponse from(Tag tag) {
			return tag != null ? new TagResponse(tag.getId(), tag.getName()) : null;
		}
	}

	public record RepeatResponse(
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			String type,
			List<Weekday> weekdays,
			Integer dayOfMonth
	) {
		public static RepeatResponse from(Todo todo) {
			RepeatType repeatType = todo.getRepeatType();
			List<Weekday> weekdays = repeatType == RepeatType.WEEKLY
					? List.copyOf(todo.getRepeatWeekdays())
					: null;
			Integer dayOfMonth = repeatType == RepeatType.MONTHLY
					? todo.getRepeatDayOfMonth()
					: null;

			return new RepeatResponse(repeatType.name(), weekdays, dayOfMonth);
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
			Objects.requireNonNull(subtask, "하위 태스크는 null일 수 없습니다.");
			return new SubtaskResponse(
					subtask.getId(),
					subtask.getContent(),
					completed
			);
		}
	}
}
