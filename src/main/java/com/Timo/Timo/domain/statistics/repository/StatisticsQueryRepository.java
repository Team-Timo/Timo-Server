package com.Timo.Timo.domain.statistics.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StatisticsQueryRepository {

	private final EntityManager entityManager;

	public Long sumDailyTimerRecordSeconds(
		Long userId,
		LocalDateTime fromInclusive,
		LocalDateTime toExclusive
	) {
		Query query = entityManager.createNativeQuery("""
				select coalesce(sum(tr.actual_seconds), 0)
				from timer_records tr
				where tr.user_id = :userId
					and tr.actual_seconds is not null
					and coalesce(tr.ended_at, tr.started_at) >= :fromInclusive
					and coalesce(tr.ended_at, tr.started_at) < :toExclusive
				""")
			.setParameter("userId", userId)
			.setParameter("fromInclusive", fromInclusive)
			.setParameter("toExclusive", toExclusive);

		return toLong(query.getSingleResult());
	}

	public List<StatisticsDailyTodo> findDailyTodos(
		Long userId,
		LocalDate date,
		LocalDateTime fromInclusive,
		LocalDateTime toExclusive
	) {
		Query query = entityManager.createNativeQuery("""
				select
					t.id as todo_id,
					t.title as title,
					coalesce(sum(tr.actual_seconds), 0) as actual_seconds,
					t.duration_seconds as estimated_seconds,
					tag.name as tag_name
				from todo_instances ti
				join todos t on t.id = ti.todo_id
				left join tags tag on tag.id = t.tag_id
				left join timer_records tr
					on tr.todo_id = t.id
					and tr.user_id = :userId
					and tr.actual_seconds is not null
					and coalesce(tr.ended_at, tr.started_at) >= :fromInclusive
					and coalesce(tr.ended_at, tr.started_at) < :toExclusive
				where t.user_id = :userId
					and ti.instance_date = :date
				group by
					t.id,
					t.title,
					t.duration_seconds,
					tag.name,
					ti.sort_order
				order by ti.sort_order asc, t.id asc
				""")
			.setParameter("userId", userId)
			.setParameter("date", date)
			.setParameter("fromInclusive", fromInclusive)
			.setParameter("toExclusive", toExclusive);

		return toDailyTodos(query.getResultList());
	}

	@SuppressWarnings("unchecked")
	private List<StatisticsDailyTodo> toDailyTodos(List<?> rows) {
		return ((List<Object[]>)rows).stream()
			.map(row -> new StatisticsDailyTodo(
				toLong(row[0]),
				(String)row[1],
				toLong(row[2]),
				toInteger(row[3]),
				(String)row[4]
			))
			.toList();
	}

	private Long toLong(Object value) {
		if (value == null) {
			return 0L;
		}
		return ((Number)value).longValue();
	}

	private Integer toInteger(Object value) {
		if (value == null) {
			return 0;
		}
		return ((Number)value).intValue();
	}
}