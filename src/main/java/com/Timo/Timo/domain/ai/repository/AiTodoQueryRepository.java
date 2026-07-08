package com.Timo.Timo.domain.ai.repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.Timo.Timo.domain.ai.dto.TodoDurationHistory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AiTodoQueryRepository {

	private final EntityManager entityManager;

	public List<TodoDurationHistory> findActualDurationHistoriesBySimilarTitle(
		Long userId,
		String title,
		LocalDate today,
		int limit
	) {
		Query query = entityManager.createNativeQuery("""
				select
					t.title,
					tr.actual_seconds,
					date(coalesce(tr.ended_at, tr.started_at)) as recorded_date
				from todos t
				join timer_records tr on tr.todo_id = t.id
				where t.user_id = :userId
					and tr.user_id = :userId
					and tr.actual_seconds is not null
					and date(coalesce(tr.ended_at, tr.started_at)) <= :today
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
					coalesce(tr.ended_at, tr.started_at) desc,
					tr.id desc
				""")
			.setParameter("userId", userId)
			.setParameter("title", title)
			.setParameter("today", today)
			.setMaxResults(limit);

		return toHistories(query.getResultList());
	}

	public List<TodoDurationHistory> findActualDurationHistoriesByTagId(
		Long userId,
		Long tagId,
		LocalDate today,
		int limit
	) {
		Query query = entityManager.createNativeQuery("""
				select
					t.title,
					tr.actual_seconds,
					date(coalesce(tr.ended_at, tr.started_at)) as recorded_date
				from todos t
				join timer_records tr on tr.todo_id = t.id
				where t.user_id = :userId
					and tr.user_id = :userId
					and tr.actual_seconds is not null
					and date(coalesce(tr.ended_at, tr.started_at)) <= :today
					and t.tag_id = :tagId
				order by coalesce(tr.ended_at, tr.started_at) desc, tr.id desc
				""")
			.setParameter("userId", userId)
			.setParameter("tagId", tagId)
			.setParameter("today", today)
			.setMaxResults(limit);

		return toHistories(query.getResultList());
	}

	@SuppressWarnings("unchecked")
	private List<TodoDurationHistory> toHistories(List<?> rows) {
		return ((List<Object[]>)rows).stream()
			.map(row -> new TodoDurationHistory(
				(String)row[0],
				toInteger(row[1]),
				toLocalDate(row[2])
			))
			.toList();
	}

	private Long toLong(Object value) {
		if (value == null) {
			return null;
		}
		return ((Number)value).longValue();
	}

	private Integer toInteger(Object value) {
		if (value == null) {
			return null;
		}
		return ((Number)value).intValue();
	}

	private LocalDate toLocalDate(Object value) {
		if (value instanceof LocalDate localDate) {
			return localDate;
		}
		if (value instanceof Date date) {
			return date.toLocalDate();
		}
		if (value instanceof Timestamp timestamp) {
			return timestamp.toLocalDateTime().toLocalDate();
		}
		return LocalDate.parse(value.toString());
	}
}
