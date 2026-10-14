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
		DayGroup group = loadDayGroup(todo, instance);
		SubtaskCompletion targetCompletion = group.completionOf(target);

		if (targetCompletion.isCompleted()) {
			throw new CustomException(TodoErrorCode.COMPLETED_SUBTASK_CANNOT_REORDER);
		}

		List<SubtaskCompletion> incomplete = group.incomplete();
		if (newIndex == null || newIndex < 0 || newIndex >= incomplete.size()) {
			throw new CustomException(TodoErrorCode.INVALID_INDEX);
		}

		incomplete.remove(targetCompletion);
		incomplete.add(newIndex, targetCompletion);

		renumber(incomplete, group.completed());
		subtaskCompletionRepository.saveAll(group.newCompletions());
		return targetCompletion;
	}

	public SubtaskCompletion applyCompletion(Todo todo, TodoInstance instance, Subtask target, boolean completed) {
		DayGroup group = loadDayGroup(todo, instance);
		SubtaskCompletion targetCompletion = group.completionOf(target);

		List<SubtaskCompletion> incomplete = group.incomplete();
		List<SubtaskCompletion> completedGroup = group.completed();
		incomplete.remove(targetCompletion);
		completedGroup.remove(targetCompletion);

		targetCompletion.updateCompleted(completed);
		if (completed) {
			completedGroup.add(targetCompletion);
		} else {
			incomplete.add(0, targetCompletion);
		}

		renumber(incomplete, completedGroup);
		subtaskCompletionRepository.saveAll(group.newCompletions());
		return targetCompletion;
	}

	private void renumber(List<SubtaskCompletion> incomplete, List<SubtaskCompletion> completed) {
		int sortOrder = 0;
		for (SubtaskCompletion completion : incomplete) {
			completion.updateSortOrder(sortOrder++);
		}
		for (SubtaskCompletion completion : completed) {
			completion.updateSortOrder(sortOrder++);
		}
	}

	// 날짜별 순서를 저장하려면 그날의 모든 하위 태스크에 레코드가 있어야 하므로, 없는 것은 미완료로 준비한다.
	// 새 레코드는 검증이 끝난 뒤에 저장해, 잘못된 요청에 INSERT가 나가지 않게 한다.
	private DayGroup loadDayGroup(Todo todo, TodoInstance instance) {
		Map<Long, SubtaskCompletion> completionsBySubtaskId = subtaskCompletionRepository
				.findAllByTodoInstanceIdForUpdate(instance.getId()).stream()
				.collect(Collectors.toMap(completion -> completion.getSubtask().getId(), Function.identity()));

		List<SubtaskCompletion> newCompletions = new ArrayList<>();
		for (Subtask subtask : todo.getSubtasks()) {
			if (!completionsBySubtaskId.containsKey(subtask.getId())) {
				SubtaskCompletion created = SubtaskCompletion.of(instance, subtask, false);
				completionsBySubtaskId.put(subtask.getId(), created);
				newCompletions.add(created);
			}
		}

		// 화면에 보이는 현재 순서(미완료 → 완료)를 기준으로 그룹을 나눈다.
		Map<Boolean, List<SubtaskCompletion>> partitioned = SubtaskStates.from(completionsBySubtaskId.values())
				.sort(todo.getSubtasks()).stream()
				.map(subtask -> completionsBySubtaskId.get(subtask.getId()))
				.collect(Collectors.partitioningBy(
						SubtaskCompletion::isCompleted,
						Collectors.toCollection(ArrayList::new)
				));

		return new DayGroup(completionsBySubtaskId, partitioned.get(false), partitioned.get(true), newCompletions);
	}

	private record DayGroup(
			Map<Long, SubtaskCompletion> completionsBySubtaskId,
			List<SubtaskCompletion> incomplete,
			List<SubtaskCompletion> completed,
			List<SubtaskCompletion> newCompletions
	) {
		SubtaskCompletion completionOf(Subtask subtask) {
			return completionsBySubtaskId.get(subtask.getId());
		}
	}
}
