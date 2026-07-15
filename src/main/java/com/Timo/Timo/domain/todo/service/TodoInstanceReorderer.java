package com.Timo.Timo.domain.todo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
			// 해당 날짜에 발생하지 않는 규칙이면 그룹에 인스턴스가 없다.
			throw new CustomException(TodoErrorCode.TODO_NOT_FOUND);
		}
		return target;
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

    int nextSortOrder = result.size();
    for (Todo rule : occurringRules) {
      if (!result.containsKey(rule.getId())) {
        TodoInstance created = todoInstanceRepository.save(TodoInstance.of(rule, date, nextSortOrder++));
        result.put(rule.getId(), created);
      }
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
