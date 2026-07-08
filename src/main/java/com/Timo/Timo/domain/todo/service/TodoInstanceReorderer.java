package com.Timo.Timo.domain.todo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.entity.TodoInstance;
import com.Timo.Timo.domain.todo.repository.TodoInstanceRepository;
import com.Timo.Timo.domain.todo.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TodoInstanceReorderer {

	private final TodoRepository todoRepository;
	private final TodoInstanceRepository todoInstanceRepository;
	private final TodoDateCalculator todoDateCalculator;

	public TodoInstance applyCompletion(Long userId, Todo targetRule, LocalDate date, boolean completed) {
		Map<Long, TodoInstance> instancesByTodoId = materializeDayGroup(userId, date);
		TodoInstance target = instancesByTodoId.get(targetRule.getId());

		if (completed) {
			moveToCompletedBottom(target, instancesByTodoId.values());
		} else {
			moveToIncompleteTop(target, instancesByTodoId.values());
		}
		return target;
	}

	private Map<Long, TodoInstance> materializeDayGroup(Long userId, LocalDate date) {
		List<Todo> occurringRules = todoRepository.findRulesInRange(userId, date, date).stream()
				.filter(rule -> todoDateCalculator.occursOn(rule, date))
				.toList();

		List<Long> todoIds = occurringRules.stream()
				.map(Todo::getId)
				.toList();

		Map<Long, TodoInstance> existing = todoIds.isEmpty()
				? Map.of()
				: todoInstanceRepository.findByTodoIdsAndDateRange(todoIds, date, date).stream()
						.collect(Collectors.toMap(
								instance -> instance.getTodo().getId(),
								Function.identity()
						));

		Map<Long, TodoInstance> result = new LinkedHashMap<>();
		for (int index = 0; index < occurringRules.size(); index++) {
			Todo rule = occurringRules.get(index);
			TodoInstance instance = existing.get(rule.getId());
			if (instance == null) {
				instance = todoInstanceRepository.save(TodoInstance.of(rule, date, index));
			}
			result.put(rule.getId(), instance);
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
