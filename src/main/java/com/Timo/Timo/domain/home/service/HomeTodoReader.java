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
		List<TodoOnDate> entries = loaded.rules().stream()
				.filter(rule -> todoDateCalculator.occursOn(rule, date))
				.map(rule -> new TodoOnDate(
						rule,
						loaded.instancesByKey().get(new InstanceKey(rule.getId(), date))
				))
				.sorted(TODO_ORDER)
				.toList();

		List<TodoResponse> todos = new ArrayList<>();
		for (int index = 0; index < entries.size(); index++) {
			TodoOnDate entry = entries.get(index);
			Todo rule = entry.rule();
			Tag tag = rule.getTagId() == null ? null : loaded.tagsById().get(rule.getTagId());
			todos.add(homeTodoMapper.toResponse(
					new TodoContext(rule, entry.instance(), tag, index)
			));
		}

		return todos;
	}

	/**
	 * 미완료 → 완료 순으로 묶고, 미완료 그룹 안에서는
	 * 인스턴스가 없는(= 아직 재정렬/완료된 적 없는, 방금 생성된) 투두를 최상단에 최신순으로,
	 * 인스턴스가 있는 투두는 기존 sortOrder 순으로 정렬한다.
	 */
	private static final Comparator<TodoOnDate> TODO_ORDER = Comparator
			.comparing(TodoOnDate::completed)
			.thenComparingInt(entry -> entry.instance() == null ? 0 : 1)
			.thenComparing(HomeTodoReader::compareWithinGroup);

	private static int compareWithinGroup(TodoOnDate a, TodoOnDate b) {
		if (a.instance() == null && b.instance() == null) {
			// 새로 생성된 투두: 최신 생성이 위로
			int byCreatedAt = b.rule().getCreatedAt().compareTo(a.rule().getCreatedAt());
			return byCreatedAt != 0 ? byCreatedAt : Long.compare(b.rule().getId(), a.rule().getId());
		}
		if (a.instance() != null && b.instance() != null) {
			// 이미 인스턴스가 있는 투두: 기존 sortOrder 순
			int bySortOrder = Integer.compare(a.instance().getSortOrder(), b.instance().getSortOrder());
			return bySortOrder != 0 ? bySortOrder : Long.compare(a.rule().getId(), b.rule().getId());
		}
		// 그룹(인스턴스 유무)이 다르면 앞 단계에서 이미 정렬됨
		return 0;
	}

	private record TodoOnDate(
			Todo rule,
			TodoInstance instance
	) {
		boolean completed() {
			return instance != null && instance.isCompleted();
		}
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
