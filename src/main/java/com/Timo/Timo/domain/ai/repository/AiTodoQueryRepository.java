package com.Timo.Timo.domain.ai.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.Timo.Timo.domain.ai.dto.TodoDurationHistory;
import com.Timo.Timo.domain.ai.dto.TodoFeedbackSource;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AiTodoQueryRepository {

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
					and (
						lower(t.title) like lower(concat('%', :title, '%'))
						or lower(:title) like lower(concat('%', t.title, '%'))
					)
				order by
					case
						when lower(t.title) = lower(:title) then 0
						when lower(t.title) like lower(concat('%', :title, '%')) then 1
						when lower(:title) like lower(concat('%', t.title, '%')) then 2
						else 3
					end,
					coalesce(tr.endedAt, tr.startedAt) desc,
					tr.id desc
				""", TodoDurationHistoryRow.class)
			.setParameter("userId", userId)
			.setParameter("title", title)
			.setParameter("toExclusive", toExclusive)
			.setMaxResults(limit)
			.getResultList();

		return toHistories(rows, userZoneId);
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

	private LocalDate toUserLocalDate(LocalDateTime utcDateTime, ZoneId userZoneId) {
		return utcDateTime
			.atZone(ZoneOffset.UTC)
			.withZoneSameInstant(userZoneId)
			.toLocalDate();
	}
}