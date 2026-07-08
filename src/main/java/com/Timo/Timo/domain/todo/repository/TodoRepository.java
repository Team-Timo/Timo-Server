package com.Timo.Timo.domain.todo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Timo.Timo.domain.statistics.repository.StatisticsDailyTodo;
import com.Timo.Timo.domain.todo.entity.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {

	Optional<Todo> findByIdAndUser_Id(Long id, Long userId);

	@Query("""
		select t from Todo t
		where t.user.id = :userId
		  and t.startDate <= :to
		  and t.endDate >= :from
		order by t.createdAt asc, t.id asc
		""")
	List<Todo> findRulesInRange(
			@Param("userId") Long userId,
			@Param("from") LocalDate from,
			@Param("to") LocalDate to
	);

	long countByUser_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
			Long userId, LocalDate to, LocalDate from
	);

	@Query("""
		select
			ti.date as date,
			count(ti.id) as totalCount,
			sum(case when ti.completed = true then 1L else 0L end) as completedCount
		from TodoInstance ti
		join ti.todo t
		where t.user.id = :userId
		  and ti.date between :from and :to
		group by ti.date
		order by ti.date asc
		""")
	List<TodoDailyCompletionStats> findDailyCompletionStats(
			@Param("userId") Long userId,
			@Param("from") LocalDate from,
			@Param("to") LocalDate to
	);

	@Query("""
		select
			count(distinct function('date', t.createdAt)) as activeDayCount,
			count(distinct case when ti.completed = true then t.id else null end) as completedTodoCount,
			count(distinct t.id) as totalTodoCount
		from Todo t
		left join TodoInstance ti on ti.todo = t
		where t.user.id = :userId
		  and t.createdAt >= :fromInclusive
		  and t.createdAt < :toExclusive
		""")
	TodoMonthlySummaryStats findMonthlySummaryStats(
			@Param("userId") Long userId,
			@Param("fromInclusive") LocalDateTime fromInclusive,
			@Param("toExclusive") LocalDateTime toExclusive
	);

	@Query("""
		select new com.Timo.Timo.domain.statistics.repository.StatisticsDailyTodo(
			t.id,
			t.title,
			coalesce(sum(tr.actualSeconds), 0),
			t.durationSeconds,
			tag.name
		)
		from TodoInstance ti
		join ti.todo t
		left join Tag tag on tag.id = t.tagId
		left join TimerRecord tr on tr.todo = t
			and tr.user.id = :userId
			and tr.actualSeconds is not null
			and coalesce(tr.endedAt, tr.startedAt) >= :fromInclusive
			and coalesce(tr.endedAt, tr.startedAt) < :toExclusive
		where t.user.id = :userId
			and ti.date = :date
		group by t.id, t.title, t.durationSeconds, tag.name, ti.sortOrder
		order by ti.sortOrder asc, t.id asc
		""")
	List<StatisticsDailyTodo> findDailyTodos(
		@Param("userId") Long userId,
		@Param("date") LocalDate date,
		@Param("fromInclusive") LocalDateTime fromInclusive,
		@Param("toExclusive") LocalDateTime toExclusive
	);
}