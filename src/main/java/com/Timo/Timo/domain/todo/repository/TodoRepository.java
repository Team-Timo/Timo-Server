package com.Timo.Timo.domain.todo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Timo.Timo.domain.todo.entity.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {

	@Query("""
		select t from Todo t
		where t.user.id = :userId
		  and t.startDate <= :to
		  and t.endDate >= :from
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
}
