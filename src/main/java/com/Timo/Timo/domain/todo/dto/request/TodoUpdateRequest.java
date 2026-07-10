package com.Timo.Timo.domain.todo.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.Timo.Timo.domain.todo.enums.RepeatType;
import com.Timo.Timo.domain.todo.enums.TodoIcon;
import com.Timo.Timo.domain.todo.enums.TodoPriority;
import com.Timo.Timo.domain.todo.enums.Weekday;
import com.Timo.Timo.domain.todo.validation.ValidTodoTitle;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record TodoUpdateRequest(
		TodoIcon icon,

		@ValidTodoTitle
		String title,

		LocalDate date,

		@Min(1)
		Integer durationSeconds,

		TodoPriority priority,

		Long tagId,

		RepeatType repeatType,

		List<Weekday> repeatWeekdays,

		@Min(1)
		@Max(31)
		Integer repeatDayOfMonth,

		@Size(max = 300)
		String memo,

		@Valid
		List<TodoSubtaskUpdateRequest> subtasks
) {
	public boolean hasNoUpdatableField() {
		return icon == null
				&& title == null
				&& date == null
				&& durationSeconds == null
				&& priority == null
				&& tagId == null
				&& repeatType == null
				&& repeatWeekdays == null
				&& repeatDayOfMonth == null
				&& memo == null
				&& subtasks == null;
	}
}
