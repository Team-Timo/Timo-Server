package com.Timo.Timo.domain.todo.vo;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.Timo.Timo.domain.todo.entity.Subtask;
import com.Timo.Timo.domain.todo.entity.SubtaskCompletion;

/**
 * 한 날짜(TODO 인스턴스)의 하위 태스크 완료 여부와 날짜별 순서.
 */
public record SubtaskStates(
		Set<Long> completedSubtaskIds,
		Map<Long, Integer> sortOrderBySubtaskId
) {

	public static final SubtaskStates EMPTY = new SubtaskStates(Set.of(), Map.of());

	// 값 객체이므로 생성 후 내부 컬렉션이 바뀌지 않도록 불변 복사본으로 보관한다.
	public SubtaskStates {
		completedSubtaskIds = Set.copyOf(completedSubtaskIds);
		sortOrderBySubtaskId = Map.copyOf(sortOrderBySubtaskId);
	}

	public static SubtaskStates from(Collection<SubtaskCompletion> completions) {
		Set<Long> completedSubtaskIds = completions.stream()
				.filter(SubtaskCompletion::isCompleted)
				.map(completion -> completion.getSubtask().getId())
				.collect(Collectors.toSet());

		Map<Long, Integer> sortOrderBySubtaskId = completions.stream()
				.filter(completion -> completion.getSortOrder() != null)
				.collect(Collectors.toMap(
						completion -> completion.getSubtask().getId(),
						SubtaskCompletion::getSortOrder
				));

		return new SubtaskStates(completedSubtaskIds, sortOrderBySubtaskId);
	}

	public boolean isCompleted(Long subtaskId) {
		return completedSubtaskIds.contains(subtaskId);
	}

	/**
	 * 미완료 → 완료 순으로 나누고, 각 그룹 안에서는 날짜별 순서가 있는 항목을 먼저, 없는 항목은 기본 순서로 뒤에 둔다.
	 */
	public List<Subtask> sort(List<Subtask> subtasks) {
		return subtasks.stream()
				.sorted(Comparator
						.comparing((Subtask subtask) -> isCompleted(subtask.getId()))
						.thenComparing(subtask -> !sortOrderBySubtaskId.containsKey(subtask.getId()))
						.thenComparingInt(subtask -> sortOrderBySubtaskId.getOrDefault(subtask.getId(), 0))
						.thenComparingInt(Subtask::getSortOrder)
						.thenComparing(Subtask::getId))
				.toList();
	}
}
