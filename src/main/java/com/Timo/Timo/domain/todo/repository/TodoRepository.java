package com.Timo.Timo.domain.todo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.enums.CalendarSource;

public interface TodoRepository extends JpaRepository<Todo, Long> {

	Optional<Todo> findByIdAndUser_Id(Long id, Long userId);

	List<Todo> findByUser_IdAndExternalEventIdIn(Long userId, Collection<String> externalEventIds);

	@Query("""
		select t from Todo t
		where t.user.id = :userId
		  and t.calendarSource = :source
		  and t.startDate between :from and :to
		""")
	List<Todo> findCalendarEventsInRange(
			@Param("userId") Long userId,
			@Param("source") CalendarSource source,
			@Param("from") LocalDate from,
			@Param("to") LocalDate to
	);

	@Query("""
		select t from Todo t
		where t.user.id = :userId
		  and t.calendarSource = :source
		  and t.startDate > :date
		""")
	List<Todo> findCalendarEventsAfter(
			@Param("userId") Long userId,
			@Param("source") CalendarSource source,
			@Param("date") LocalDate date
	);

	@Modifying(flushAutomatically = true)
	@Query("""
		update Todo t
		set t.disconnectedFromCalendar = true, t.updatedAt = :now
		where t.user.id = :userId
		  and t.calendarSource = :source
		  and t.startDate <= :date
		  and t.disconnectedFromCalendar = false
		""")
	int markDisconnectedFromCalendarUpTo(
			@Param("userId") Long userId,
			@Param("source") CalendarSource source,
			@Param("date") LocalDate date,
			@Param("now") LocalDateTime now
	);

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

	@Modifying(flushAutomatically = true)
	@Query("""
		update Todo t
		set t.disconnectedFromCalendar = false, t.updatedAt = :now
		where t.user.id = :userId
		  and t.calendarSource = :source
		  and t.disconnectedFromCalendar = true
		""")
	int clearDisconnectedFromCalendar(
			@Param("userId") Long userId,
			@Param("source") CalendarSource source,
			@Param("now") LocalDateTime now
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update Todo t set t.tagId = null, t.updatedAt = :now where t.tagId = :tagId")
	int clearTagByTagId(@Param("tagId") Long tagId, @Param("now") LocalDateTime now);
}
