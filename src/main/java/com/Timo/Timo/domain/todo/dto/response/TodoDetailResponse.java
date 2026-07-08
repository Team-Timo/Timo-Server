package com.Timo.Timo.domain.todo.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.Timo.Timo.domain.tag.entity.Tag;
import com.Timo.Timo.domain.todo.entity.Subtask;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.entity.TodoInstance;
import com.Timo.Timo.domain.todo.enums.RepeatType;
import com.Timo.Timo.domain.todo.enums.TodoTimerStatus;
import com.Timo.Timo.domain.todo.enums.Weekday;

public record TodoDetailResponse(
		Long todoId,
		String icon,
		String title,
		boolean completed,
		LocalDate date,
		String dayOfWeek,
		Integer durationSeconds,
		String priority,
		TagResponse tag,
		RepeatResponse repeat,
		TodoTimerStatus timerStatus,
		String memo,
		Integer sortOrder,
		List<SubtaskResponse> subtasks
) {

	public static TodoDetailResponse of(Todo todo, TodoInstance instance, Tag tag) {
		LocalDate date = todo.getStartDate();

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
				todo.getMemo(),
				instance != null ? instance.getSortOrder() : null,
				todo.getSubtasks().stream()
						.map(SubtaskResponse::from)
						.toList()
		);
	}

	public record TagResponse(
			Long tagId,
			String name
	) {
		public static TagResponse from(Tag tag) {
			return tag != null ? new TagResponse(tag.getId(), tag.getName()) : null;
		}
	}

	public record RepeatResponse(
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
			Long subtaskId,
			String content,
			boolean completed
	) {
		public static SubtaskResponse from(Subtask subtask) {
			Objects.requireNonNull(subtask, "subtask must not be null");
			return new SubtaskResponse(
					subtask.getId(),
					subtask.getContent(),
					subtask.isCompleted()
			);
		}
	}
}
