package com.Timo.Timo.domain.todo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
