package com.Timo.Timo.domain.todo.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.Timo.Timo.domain.todo.enums.RepeatType;
import com.Timo.Timo.domain.todo.enums.TodoIcon;
import com.Timo.Timo.domain.todo.enums.TodoPriority;
import com.Timo.Timo.domain.todo.enums.Weekday;
import com.Timo.Timo.domain.todo.validation.ValidSubtaskContent;
import com.Timo.Timo.domain.todo.validation.ValidTodoTitle;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TodoCreateRequest(
		TodoIcon icon,

		@NotBlank
		@ValidTodoTitle
		String title,

		List<@NotBlank @ValidSubtaskContent String> subtasks,

		@NotNull
		LocalDate date,

		@NotBlank
		String duration,

		TodoPriority priority,

		Long tagId,

		@NotNull
		RepeatType repeatType,

		List<Weekday> repeatWeekdays,

		@Min(1)
		@Max(31)
		Integer repeatDayOfMonth,

		@Size(max = 300)
		String memo
) {

	@AssertTrue(message = "WEEKLY는 요일, MONTHLY는 일자가 필요합니다.")
	public boolean isRepeatRuleValid() {
		if (repeatType == RepeatType.WEEKLY) {
			return repeatWeekdays != null && !repeatWeekdays.isEmpty();
		}
		if (repeatType == RepeatType.MONTHLY) {
			return repeatDayOfMonth != null;
		}
		return true;
	}
}
