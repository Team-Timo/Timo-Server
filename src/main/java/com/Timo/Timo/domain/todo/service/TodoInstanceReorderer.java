package com.Timo.Timo.domain.todo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.entity.TodoInstance;
import com.Timo.Timo.domain.todo.exception.TodoErrorCode;
import com.Timo.Timo.domain.todo.repository.TodoInstanceRepository;
import com.Timo.Timo.domain.todo.repository.TodoRepository;
import com.Timo.Timo.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TodoInstanceReorderer {

	private final TodoRepository todoRepository;
	private final TodoInstanceRepository todoInstanceRepository;
	private final TodoDateCalculator todoDateCalculator;

	public TodoInstance applyCompletion(Long userId, Todo targetRule, LocalDate date, boolean completed) {
		Map<Long, TodoInstance> instancesByTodoId = materializeDayGroup(userId, date);
		TodoInstance target = requireTarget(instancesByTodoId, targetRule);

		if (completed) {
			moveToCompletedBottom(target, instancesByTodoId.values());
		} else {
			moveToIncompleteTop(target, instancesByTodoId.values());
		}
		return target;
	}

	public TodoInstance applyReorder(Long userId, Todo targetRule, LocalDate date, Integer newIndex) {
		Map<Long, TodoInstance> instancesByTodoId = materializeDayGroup(userId, date);
		TodoInstance target = requireTarget(instancesByTodoId, targetRule);

		if (target.isCompleted()) {
			throw new CustomException(TodoErrorCode.COMPLETED_CANNOT_REORDER);
		}

		List<TodoInstance> incomplete = instancesByTodoId.values().stream()
				.filter(instance -> !instance.isCompleted())
				.sorted(Comparator.comparingInt(TodoInstance::getSortOrder))
				.collect(Collectors.toCollection(ArrayList::new));

		if (newIndex == null || newIndex < 0 || newIndex >= incomplete.size()) {
			throw new CustomException(TodoErrorCode.INVALID_INDEX);
		}

		incomplete.remove(target);
		incomplete.add(newIndex, target);

		List<TodoInstance> completed = instancesByTodoId.values().stream()
				.filter(TodoInstance::isCompleted)
				.sorted(Comparator.comparingInt(TodoInstance::getSortOrder))
				.toList();

		int sortOrder = 0;
		for (TodoInstance instance : incomplete) {
			instance.updateSortOrder(sortOrder++);
		}
		for (TodoInstance instance : completed) {
			instance.updateSortOrder(sortOrder++);
		}
		return target;
	}

	private TodoInstance requireTarget(Map<Long, TodoInstance> instancesByTodoId, Todo targetRule) {
		TodoInstance target = instancesByTodoId.get(targetRule.getId());
		if (target == null) {
			throw new CustomException(TodoErrorCode.TODO_NOT_FOUND);
		}
		return target;
	}

	public TodoInstance materializeInstance(Long userId, Todo rule, LocalDate date) {
		// 이미 인스턴스가 있으면 그대로 반환한다. (상태 변경 시마다 불필요한 그룹 정규화를 피함)
		return todoInstanceRepository.findByTodo_IdAndDate(rule.getId(), date)
				.orElseGet(() -> {
					// 신규 생성이 필요할 때만 그룹을 정규화해 정렬 일관성을 유지한다.
					Map<Long, TodoInstance> group = materializeDayGroup(userId, date);
					TodoInstance materialized = group.get(rule.getId());
					if (materialized != null) {
						return materialized;
					}
					// 해당 날짜에 규칙이 발생하지 않아 그룹에 없는 경우: 정렬 그룹과 무관하게 개별 생성한다.
					return todoInstanceRepository.save(TodoInstance.of(rule, date, 0));
				});
	}

	private Map<Long, TodoInstance> materializeDayGroup(Long userId, LocalDate date) {
    Map<Long, TodoInstance> result = new LinkedHashMap<>();

    List<TodoInstance> existingInstances = todoInstanceRepository.findDailyInstances(userId, date);
    for (TodoInstance instance : existingInstances) {
      result.put(instance.getTodo().getId(), instance);
    }

    List<Todo> occurringRules = todoRepository.findRulesInRange(userId, date, date).stream()
        .filter(rule -> todoDateCalculator.occursOn(rule, date))
        .toList();

    List<TodoInstance> newInstances = occurringRules.stream()
        .filter(rule -> !result.containsKey(rule.getId()))
        .sorted(Comparator.comparing(Todo::getCreatedAt).thenComparing(Todo::getId).reversed())
        .map(rule -> todoInstanceRepository.save(TodoInstance.of(rule, date, 0)))
        .toList();

    if (newInstances.isEmpty()) {
      return result;
    }

    List<TodoInstance> existingIncomplete = existingInstances.stream()
        .filter(instance -> !instance.isCompleted())
        .sorted(Comparator.comparingInt(TodoInstance::getSortOrder))
        .toList();
    List<TodoInstance> existingCompleted = existingInstances.stream()
        .filter(TodoInstance::isCompleted)
        .sorted(Comparator.comparingInt(TodoInstance::getSortOrder))
        .toList();

    int sortOrder = 0;
    for (TodoInstance instance : newInstances) {
      instance.updateSortOrder(sortOrder++);
      result.put(instance.getTodo().getId(), instance);
    }
    for (TodoInstance instance : existingIncomplete) {
      instance.updateSortOrder(sortOrder++);
    }
    for (TodoInstance instance : existingCompleted) {
      instance.updateSortOrder(sortOrder++);
    }

    return result;
	}

	private void moveToCompletedBottom(TodoInstance target, Iterable<TodoInstance> group) {
		target.markCompleted();

		int maxCompletedSortOrder = -1;
		for (TodoInstance instance : group) {
			if (instance != target && instance.isCompleted()) {
				maxCompletedSortOrder = Math.max(maxCompletedSortOrder, instance.getSortOrder());
			}
		}
		target.updateSortOrder(maxCompletedSortOrder + 1);
	}

	private void moveToIncompleteTop(TodoInstance target, Iterable<TodoInstance> group) {
		target.markIncomplete();

		List<TodoInstance> others = new ArrayList<>();
		for (TodoInstance instance : group) {
			if (instance != target && !instance.isCompleted()) {
				others.add(instance);
			}
		}
		others.forEach(instance -> instance.updateSortOrder(instance.getSortOrder() + 1));
		target.updateSortOrder(0);
	}
}
