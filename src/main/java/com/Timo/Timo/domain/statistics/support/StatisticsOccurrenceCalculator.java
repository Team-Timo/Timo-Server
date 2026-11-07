package com.Timo.Timo.domain.statistics.support;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.entity.TodoInstance;
import com.Timo.Timo.domain.todo.repository.TodoInstanceRepository;
import com.Timo.Timo.domain.todo.repository.TodoRepository;
import com.Timo.Timo.domain.todo.service.TodoDateCalculator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatisticsOccurrenceCalculator {

	private static final Comparator<CompletedTodo> COMPLETED_TODO_ORDER = Comparator
		.comparing((CompletedTodo completedTodo) -> completedTodo.instance().getSortOrder())
		.thenComparing(completedTodo -> completedTodo.rule().getId());

	private final TodoRepository todoRepository;
	private final TodoInstanceRepository todoInstanceRepository;
	private final TodoDateCalculator todoDateCalculator;

	public Map<LocalDate, DailyOccurrence> calculateDailyOccurrences(Long userId, LocalDate from, LocalDate to) {
		List<Todo> rules = todoRepository.findRulesInRange(userId, from, to);
		Map<InstanceKey, TodoInstance> instancesByKey = loadInstances(rules, from, to);

		return from.datesUntil(to.plusDays(1))
			.collect(Collectors.toMap(
				Function.identity(),
				date -> summarizeDate(rules, instancesByKey, date)
			));
	}

	public List<Todo> findDailyCompletedTodos(Long userId, LocalDate date) {
		List<Todo> rules = todoRepository.findRulesInRange(userId, date, date);
		Map<InstanceKey, TodoInstance> instancesByKey = loadInstances(rules, date, date);

		return rules.stream()
			.map(rule -> new CompletedTodo(rule, instancesByKey.get(new InstanceKey(rule.getId(), date))))
			.filter(completedTodo -> completedTodo.instance() != null && completedTodo.instance().isCompleted())
			.sorted(COMPLETED_TODO_ORDER)
			.map(CompletedTodo::rule)
			.toList();
	}

	private DailyOccurrence summarizeDate(List<Todo> rules, Map<InstanceKey, TodoInstance> instancesByKey, LocalDate date) {
		int totalCount = 0;
		Set<Long> completedTodoIds = new HashSet<>();

		for (Todo rule : rules) {
			TodoInstance instance = instancesByKey.get(new InstanceKey(rule.getId(), date));
			boolean occursByCurrentRule = todoDateCalculator.occursOn(rule, date);
			if (!occursByCurrentRule && instance == null) {
				continue;
			}
			totalCount++;
			if (instance != null && instance.isCompleted()) {
				completedTodoIds.add(rule.getId());
			}
		}

		return new DailyOccurrence(date, totalCount, completedTodoIds.size(), completedTodoIds);
	}

	private Map<InstanceKey, TodoInstance> loadInstances(List<Todo> rules, LocalDate from, LocalDate to) {
		List<Long> todoIds = rules.stream().map(Todo::getId).toList();
		if (todoIds.isEmpty()) {
			return Map.of();
		}

		return todoInstanceRepository.findByTodoIdsAndDateRange(todoIds, from, to).stream()
			.collect(Collectors.toMap(
				instance -> new InstanceKey(instance.getTodo().getId(), instance.getDate()),
				Function.identity()
			));
	}

	public record DailyOccurrence(LocalDate date, int totalCount, int completedCount, Set<Long> completedTodoIds) {
	}

	private record InstanceKey(Long todoId, LocalDate date) {
	}

	private record CompletedTodo(Todo rule, TodoInstance instance) {
	}
}