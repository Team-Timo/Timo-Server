package com.Timo.Timo.domain.home.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.home.dto.response.HomeResponse.TodoResponse;
import com.Timo.Timo.domain.home.mapper.HomeTodoMapper;
import com.Timo.Timo.domain.home.mapper.HomeTodoMapper.TodoContext;
import com.Timo.Timo.domain.tag.entity.Tag;
import com.Timo.Timo.domain.tag.repository.TagRepository;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.entity.TodoInstance;
import com.Timo.Timo.domain.todo.repository.TodoInstanceRepository;
import com.Timo.Timo.domain.todo.repository.TodoRepository;
import com.Timo.Timo.domain.todo.service.TodoDateCalculator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HomeTodoReader {

	private final TodoRepository todoRepository;
	private final TodoInstanceRepository todoInstanceRepository;
	private final TagRepository tagRepository;
	private final TodoDateCalculator todoDateCalculator;
	private final HomeTodoMapper homeTodoMapper;

	public LoadedTodos load(Long userId, LocalDate from, LocalDate to) {
		List<Todo> rules = todoRepository.findRulesInRange(userId, from, to);
		return new LoadedTodos(rules, loadInstances(rules, from, to), loadTags(rules));
	}

	public List<TodoResponse> sortedTodosOn(LoadedTodos loaded, LocalDate date) {
		List<Todo> occurredRules = loaded.rules().stream()
				.filter(rule -> todoDateCalculator.occursOn(rule, date))
				.toList();

		List<TodoResponse> todos = new ArrayList<>();
		for (int index = 0; index < occurredRules.size(); index++) {
			Todo rule = occurredRules.get(index);
			TodoInstance instance = loaded.instancesByKey().get(new InstanceKey(rule.getId(), date));
			todos.add(homeTodoMapper.toResponse(
					new TodoContext(rule, instance, loaded.tagsById().get(rule.getTagId()), index)
			));
		}

		return todos.stream()
				.sorted(Comparator
						.comparing(TodoResponse::completed)
						.thenComparing(TodoResponse::sortOrder, Comparator.nullsLast(Integer::compareTo))
						.thenComparing(TodoResponse::todoId))
				.toList();
	}

	private Map<InstanceKey, TodoInstance> loadInstances(List<Todo> rules, LocalDate from, LocalDate to) {
		List<Long> todoIds = rules.stream()
				.map(Todo::getId)
				.toList();

		if (todoIds.isEmpty()) {
			return Map.of();
		}

		return todoInstanceRepository.findByTodoIdsAndDateRange(todoIds, from, to).stream()
				.collect(Collectors.toMap(
						instance -> new InstanceKey(instance.getTodo().getId(), instance.getDate()),
						Function.identity()
				));
	}

	private Map<Long, Tag> loadTags(List<Todo> rules) {
		List<Long> tagIds = rules.stream()
				.map(Todo::getTagId)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

		if (tagIds.isEmpty()) {
			return Map.of();
		}

		return tagRepository.findAllById(tagIds).stream()
				.collect(Collectors.toMap(Tag::getId, Function.identity()));
	}

	public record LoadedTodos(
			List<Todo> rules,
			Map<InstanceKey, TodoInstance> instancesByKey,
			Map<Long, Tag> tagsById
	) {
	}

	private record InstanceKey(
			Long todoId,
			LocalDate date
	) {
	}
}
