package com.Timo.Timo.domain.ai.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.ai.dto.TodoDurationHistory;
import com.Timo.Timo.domain.ai.dto.TodoFeedbackSource;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AiTodoQueryRepository {

	private static final int CANDIDATE_WINDOW = 200;
	private static final int UNMATCHED_PRIORITY = 3;

	private final EntityManager entityManager;

	@Transactional(readOnly = true)
	public TodoFeedbackSource findFeedbackSource(Long userId, Long todoId) {
		List<TodoFeedbackSource> sources = entityManager.createQuery("""
				select new com.Timo.Timo.domain.ai.dto.TodoFeedbackSource(
					t.title,
					t.tagId,
					tag.name,
					t.durationSeconds,
					0
				)
				from Todo t
				left join Tag tag on tag.id = t.tagId
				where t.id = :todoId
					and t.user.id = :userId
				""", TodoFeedbackSource.class)
			.setParameter("userId", userId)
			.setParameter("todoId", todoId)
			.getResultList();

		if (sources.isEmpty()) {
			return null;
		}

		TodoFeedbackSource source = sources.get(0);
		return new TodoFeedbackSource(
			source.title(),
			source.tagId(),
			source.tagName(),
			source.estimatedSeconds(),
			findLatestActualSeconds(userId, todoId)
		);
	}

	public List<TodoDurationHistory> findActualDurationHistoriesBySimilarTitle(
		Long userId,
		String title,
		LocalDateTime toExclusive,
		ZoneId userZoneId,
		int limit
	) {
		String trimmedTitle = title == null ? "" : title.trim();
		String normalizedSearchTitle = normalize(title);

		List<TodoDurationHistoryRow> exactMatches = findExactMatchCandidates(userId, trimmedTitle, toExclusive, limit);

		int remaining = limit - exactMatches.size();
		Set<Long> exactMatchIds = exactMatches.stream()
			.map(TodoDurationHistoryRow::timerRecordId)
			.collect(Collectors.toSet());
		List<TodoDurationHistoryRow> partialMatches = remaining > 0
			? findPartialMatchCandidates(userId, normalizedSearchTitle, exactMatchIds, toExclusive, remaining)
			: List.of();

		return toHistories(
			Stream.concat(exactMatches.stream(), partialMatches.stream()).toList(),
			userZoneId
		);
	}

	private List<TodoDurationHistoryRow> findExactMatchCandidates(
		Long userId,
		String trimmedTitle,
		LocalDateTime toExclusive,
		int limit
	) {
		if (trimmedTitle.isBlank() || limit <= 0) {
			return List.of();
		}

		return entityManager.createQuery("""
				select new com.Timo.Timo.domain.ai.repository.TodoDurationHistoryRow(
					tr.id,
					t.title,
					tr.actualSeconds,
					tr.endedAt
				)
				from TimerRecord tr
				join tr.todo t
				where t.user.id = :userId
					and t.title = :title
					and tr.actualSeconds is not null
					and tr.endedAt < :toExclusive
				order by tr.endedAt desc, tr.id desc
				""", TodoDurationHistoryRow.class)
			.setParameter("userId", userId)
			.setParameter("title", trimmedTitle)
			.setParameter("toExclusive", toExclusive)
			.setMaxResults(limit)
			.getResultList();
	}

	private List<TodoDurationHistoryRow> findPartialMatchCandidates(
		Long userId,
		String normalizedSearchTitle,
		Set<Long> excludedIds,
		LocalDateTime toExclusive,
		int limit
	) {
		if (limit <= 0) {
			return List.of();
		}

		List<TodoDurationHistoryRow> candidates = entityManager.createQuery("""
				select new com.Timo.Timo.domain.ai.repository.TodoDurationHistoryRow(
					tr.id,
					t.title,
					tr.actualSeconds,
					tr.endedAt
				)
				from TimerRecord tr
				join tr.todo t
				where tr.user.id = :userId
					and tr.actualSeconds is not null
					and tr.endedAt < :toExclusive
				order by tr.endedAt desc, tr.id desc
				""", TodoDurationHistoryRow.class)
			.setParameter("userId", userId)
			.setParameter("toExclusive", toExclusive)
			.setMaxResults(CANDIDATE_WINDOW)
			.getResultList();

		return candidates.stream()
			.filter(row -> !excludedIds.contains(row.timerRecordId()))
			.map(row -> new ScoredCandidate(
				row,
				matchPriority(normalize(row.title()), normalizedSearchTitle)
			))
			.filter(scored -> scored.priority() < UNMATCHED_PRIORITY)
			.sorted(Comparator.comparingInt(ScoredCandidate::priority)
				.thenComparing(scored -> scored.row().recordedAt(), Comparator.reverseOrder()))
			.limit(limit)
			.map(ScoredCandidate::row)
			.toList();
	}

	private int matchPriority(String candidateTitle, String searchTitle) {
		if (candidateTitle.equals(searchTitle)) {
			return 0;
		}
		if (candidateTitle.contains(searchTitle)) {
			return 1;
		}
		if (searchTitle.contains(candidateTitle)) {
			return 2;
		}
		return UNMATCHED_PRIORITY;
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase();
	}

	private record ScoredCandidate(TodoDurationHistoryRow row, int priority) {
	}

	public List<TodoDurationHistory> findActualDurationHistoriesByTagId(
		Long userId,
		Long tagId,
		LocalDateTime toExclusive,
		ZoneId userZoneId,
		int limit
	) {
		List<TodoDurationHistoryRow> rows = entityManager.createQuery("""
				select new com.Timo.Timo.domain.ai.repository.TodoDurationHistoryRow(
					tr.id,
					t.title,
					tr.actualSeconds,
					tr.endedAt
				)
				from TimerRecord tr
				join tr.todo t
				where t.user.id = :userId
					and tr.user.id = :userId
					and tr.actualSeconds is not null
					and tr.endedAt < :toExclusive
					and t.tagId = :tagId
				order by tr.endedAt desc, tr.id desc
				""", TodoDurationHistoryRow.class)
			.setParameter("userId", userId)
			.setParameter("tagId", tagId)
			.setParameter("toExclusive", toExclusive)
			.setMaxResults(limit)
			.getResultList();

		return toHistories(rows, userZoneId);
	}

	private Integer findLatestActualSeconds(Long userId, Long todoId) {
		return entityManager.createQuery("""
				select tr.actualSeconds
				from TimerRecord tr
				where tr.todo.id = :todoId
					and tr.user.id = :userId
					and tr.actualSeconds is not null
				order by coalesce(tr.endedAt, tr.startedAt) desc, tr.id desc
				""", Integer.class)
			.setParameter("userId", userId)
			.setParameter("todoId", todoId)
			.setMaxResults(1)
			.getResultStream()
			.findFirst()
			.orElse(0);
	}

	private List<TodoDurationHistory> toHistories(List<TodoDurationHistoryRow> rows, ZoneId userZoneId) {
		return rows.stream()
			.map(row -> new TodoDurationHistory(
				row.title(),
				row.actualSeconds(),
				toUserLocalDate(row.recordedAt(), userZoneId)
			))
			.toList();
	}

	private LocalDate toUserLocalDate(LocalDateTime utcDateTime, ZoneId userZoneId) {
		return utcDateTime
			.atZone(ZoneOffset.UTC)
			.withZoneSameInstant(userZoneId)
			.toLocalDate();
	}
}