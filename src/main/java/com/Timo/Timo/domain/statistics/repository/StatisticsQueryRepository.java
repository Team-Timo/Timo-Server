package com.Timo.Timo.domain.statistics.repository;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StatisticsQueryRepository {

	private final EntityManager entityManager;

	public StatisticsSummary findSummary(
		Long userId,
		LocalDateTime fromInclusive,
		LocalDateTime toExclusive
	) {
		return new StatisticsSummary(
			sumTimerRecordSeconds(userId, fromInclusive, toExclusive),
			countTimerRecordedDays(userId, fromInclusive, toExclusive),
			countActiveTodoCreatedDays(userId, fromInclusive, toExclusive),
			countCompletedCreatedTodos(userId, fromInclusive, toExclusive),
			countCreatedTodos(userId, fromInclusive, toExclusive)
		);
	}

	private Long sumTimerRecordSeconds(Long userId, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
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

	private Integer countTimerRecordedDays(Long userId, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		Query query = entityManager.createNativeQuery("""
				select count(distinct date(coalesce(tr.ended_at, tr.started_at)))
				from timer_records tr
				where tr.user_id = :userId
					and tr.actual_seconds is not null
					and coalesce(tr.ended_at, tr.started_at) >= :fromInclusive
					and coalesce(tr.ended_at, tr.started_at) < :toExclusive
				""")
			.setParameter("userId", userId)
			.setParameter("fromInclusive", fromInclusive)
			.setParameter("toExclusive", toExclusive);

		return toInteger(query.getSingleResult());
	}

	private Integer countActiveTodoCreatedDays(Long userId, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		Query query = entityManager.createNativeQuery("""
				select count(distinct date(t.created_at))
				from todos t
				where t.user_id = :userId
					and t.created_at >= :fromInclusive
					and t.created_at < :toExclusive
				""")
			.setParameter("userId", userId)
			.setParameter("fromInclusive", fromInclusive)
			.setParameter("toExclusive", toExclusive);

		return toInteger(query.getSingleResult());
	}

	private Integer countCompletedCreatedTodos(Long userId, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		Query query = entityManager.createNativeQuery("""
				select count(distinct t.id)
				from todos t
				join todo_instances ti on ti.todo_id = t.id
				where t.user_id = :userId
					and t.created_at >= :fromInclusive
					and t.created_at < :toExclusive
					and ti.completed = true
				""")
			.setParameter("userId", userId)
			.setParameter("fromInclusive", fromInclusive)
			.setParameter("toExclusive", toExclusive);

		return toInteger(query.getSingleResult());
	}

	private Integer countCreatedTodos(Long userId, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		Query query = entityManager.createNativeQuery("""
				select count(t.id)
				from todos t
				where t.user_id = :userId
					and t.created_at >= :fromInclusive
					and t.created_at < :toExclusive
				""")
			.setParameter("userId", userId)
			.setParameter("fromInclusive", fromInclusive)
			.setParameter("toExclusive", toExclusive);

		return toInteger(query.getSingleResult());
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
