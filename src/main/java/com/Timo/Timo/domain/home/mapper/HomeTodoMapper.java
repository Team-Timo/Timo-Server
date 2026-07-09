package com.Timo.Timo.domain.home.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.home.dto.response.HomeResponse.SubtaskResponse;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.TagResponse;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.TodoResponse;
import com.Timo.Timo.domain.tag.entity.Tag;
import com.Timo.Timo.domain.todo.entity.Subtask;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.entity.TodoInstance;
import com.Timo.Timo.domain.todo.enums.RepeatType;
import com.Timo.Timo.domain.todo.enums.TodoTimerStatus;

@Component
public class HomeTodoMapper {

	public TodoResponse toResponse(TodoContext context) {
		Todo todo = context.todo();
		TodoInstance instance = context.instance();

		return new TodoResponse(
				todo.getId(),
				todo.getIcon() != null ? todo.getIcon().name() : null,
				todo.getTitle(),
				instance != null && instance.isCompleted(),
				todo.getDurationSeconds(),
				todo.getPriority() != null ? todo.getPriority().name() : null,
				mapTag(context.tag()),
				todo.getMemo() != null && !todo.getMemo().isBlank(),
				todo.getRepeatType() != RepeatType.NONE,
				resolveTimerStatus(instance),
				instance != null ? instance.getSortOrder() : context.defaultSortOrder(),
				mapSubtasks(todo.getSubtasks())
		);
	}

	private TagResponse mapTag(Tag tag) {
		return tag != null ? TagResponse.from(tag) : null;
	}

	private TodoTimerStatus resolveTimerStatus(TodoInstance instance) {
		return instance != null ? instance.getTimerStatus() : TodoTimerStatus.STOPPED;
	}

	private List<SubtaskResponse> mapSubtasks(List<Subtask> subtasks) {
		return subtasks.stream()
				.map(SubtaskResponse::from)
				.toList();
	}

	public record TodoContext(
			Todo todo,
			TodoInstance instance,
			Tag tag,
			int defaultSortOrder
	) {
	}
}
