package com.Timo.Timo.domain.todo.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Timo.Timo.domain.todo.entity.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {

	long countByUser_IdAndDate(Long userId, LocalDate date);

	@Query("""
		select coalesce(max(t.sortOrder), 0)
		from Todo t
		where t.user.id = :userId and t.date = :date
		""")
	int findMaxSortOrderByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
