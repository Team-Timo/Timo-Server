package com.Timo.Timo.domain.ai.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.Timo.Timo.domain.ai.dto.TodoDurationHistory;
import com.Timo.Timo.domain.ai.dto.TodoFeedbackSource;
import com.Timo.Timo.domain.timer.entity.TimerRecord;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AiTodoQueryRepository {

	private static final int CANDIDATE_WINDOW = 30;
	private static final int UNMATCHED_PRIORITY = 3;

	private final EntityManager entityManager;

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
		List<TimerRecord> candidates = entityManager.createQuery("""
				select tr
				from TimerRecord tr
				join fetch tr.todo t
				where tr.user.id = :userId
					and tr.actualSeconds is not null
					and tr.endedAt < :toExclusive
				order by tr.endedAt desc, tr.id desc
				""", TimerRecord.class)
			.setParameter("userId", userId)
			.setParameter("toExclusive", toExclusive)
			.setMaxResults(CANDIDATE_WINDOW)
			.getResultList();

		String normalizedSearchTitle = normalize(title);

		return candidates.stream()
			.map(record -> new ScoredCandidate(
				record,
				matchPriority(normalize(record.getTodo().getTitle()), normalizedSearchTitle)
			))
			.filter(scored -> scored.priority() < UNMATCHED_PRIORITY)
			.sorted(Comparator.comparingInt(ScoredCandidate::priority)
				.thenComparing(scored -> scored.record().getEndedAt(), Comparator.reverseOrder()))
			.limit(limit)
			.map(scored -> toHistory(scored.record(), userZoneId))
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

	private record ScoredCandidate(TimerRecord record, int priority) {
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
					t.title,
					tr.actualSeconds,
					coalesce(tr.endedAt, tr.startedAt)
				)
				from TimerRecord tr
				join tr.todo t
				where t.user.id = :userId
					and tr.user.id = :userId
					and tr.actualSeconds is not null
					and coalesce(tr.endedAt, tr.startedAt) < :toExclusive
					and t.tagId = :tagId
				order by coalesce(tr.endedAt, tr.startedAt) desc, tr.id desc
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

	private TodoDurationHistory toHistory(TimerRecord record, ZoneId userZoneId) {
		return new TodoDurationHistory(
			record.getTodo().getTitle(),
			record.getActualSeconds(),
			toUserLocalDate(record.getEndedAt(), userZoneId)
		);
	}

	private LocalDate toUserLocalDate(LocalDateTime utcDateTime, ZoneId userZoneId) {
		return utcDateTime
			.atZone(ZoneOffset.UTC)
			.withZoneSameInstant(userZoneId)
			.toLocalDate();
	}
}