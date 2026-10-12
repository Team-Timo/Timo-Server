package com.Timo.Timo.domain.todo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.todo.entity.Subtask;
import com.Timo.Timo.domain.todo.entity.SubtaskCompletion;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.entity.TodoInstance;
import com.Timo.Timo.domain.todo.exception.TodoErrorCode;
import com.Timo.Timo.domain.todo.repository.SubtaskCompletionRepository;
import com.Timo.Timo.domain.todo.vo.SubtaskStates;
import com.Timo.Timo.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubtaskReorderer {

	private final SubtaskCompletionRepository subtaskCompletionRepository;

	public SubtaskCompletion applyReorder(Todo todo, TodoInstance instance, Subtask target, Integer newIndex) {
		Map<Long, SubtaskCompletion> completionsBySubtaskId = materializeCompletions(todo, instance);
		SubtaskCompletion targetCompletion = completionsBySubtaskId.get(target.getId());

		if (targetCompletion.isCompleted()) {
			throw new CustomException(TodoErrorCode.COMPLETED_SUBTASK_CANNOT_REORDER);
		}

		// 화면에 보이는 현재 순서(미완료 → 완료)를 기준으로 미완료 그룹 안에서만 이동한다.
		List<SubtaskCompletion> ordered = SubtaskStates.from(completionsBySubtaskId.values())
				.sort(todo.getSubtasks()).stream()
				.map(subtask -> completionsBySubtaskId.get(subtask.getId()))
				.toList();

		List<SubtaskCompletion> incomplete = ordered.stream()
				.filter(completion -> !completion.isCompleted())
				.collect(Collectors.toCollection(ArrayList::new));
		List<SubtaskCompletion> completed = ordered.stream()
				.filter(SubtaskCompletion::isCompleted)
				.toList();

		if (newIndex == null || newIndex < 0 || newIndex >= incomplete.size()) {
			throw new CustomException(TodoErrorCode.INVALID_INDEX);
		}

		incomplete.remove(targetCompletion);
		incomplete.add(newIndex, targetCompletion);

		int sortOrder = 0;
		for (SubtaskCompletion completion : incomplete) {
			completion.updateSortOrder(sortOrder++);
		}
		for (SubtaskCompletion completion : completed) {
			completion.updateSortOrder(sortOrder++);
		}
		return targetCompletion;
	}

	// 날짜별 순서를 저장하려면 그날의 모든 하위 태스크에 레코드가 있어야 하므로, 없는 것은 미완료로 만든다.
	private Map<Long, SubtaskCompletion> materializeCompletions(Todo todo, TodoInstance instance) {
		Map<Long, SubtaskCompletion> completionsBySubtaskId = subtaskCompletionRepository
				.findByTodoInstance_IdIn(List.of(instance.getId())).stream()
				.collect(Collectors.toMap(completion -> completion.getSubtask().getId(), Function.identity()));

		List<SubtaskCompletion> created = todo.getSubtasks().stream()
				.filter(subtask -> !completionsBySubtaskId.containsKey(subtask.getId()))
				.map(subtask -> SubtaskCompletion.of(instance, subtask, false))
				.toList();
		subtaskCompletionRepository.saveAll(created)
				.forEach(completion -> completionsBySubtaskId.put(completion.getSubtask().getId(), completion));

		return completionsBySubtaskId;
	}
}
