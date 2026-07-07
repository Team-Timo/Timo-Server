package com.Timo.Timo.domain.todo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Timo.Timo.domain.todo.entity.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {

	@Query("""
		SELECT
			t.scheduledDate AS date,
			COUNT(t) AS totalCount,
			SUM(CASE WHEN t.completed = true THEN 1 ELSE 0 END) AS completedCount
		FROM Todo t
		WHERE t.userId = :userId
			AND t.scheduledDate BETWEEN :startDate AND :endDate
		GROUP BY t.scheduledDate
		""")
	List<TodoDailyCompletionStats> findDailyCompletionStats(
		@Param("userId") Long userId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);
}