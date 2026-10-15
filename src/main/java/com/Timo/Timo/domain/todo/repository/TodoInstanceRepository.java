package com.Timo.Timo.domain.todo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Timo.Timo.domain.todo.entity.TodoInstance;

import jakarta.persistence.LockModeType;

public interface TodoInstanceRepository extends JpaRepository<TodoInstance, Long> {

    Optional<TodoInstance> findByTodo_IdAndDate(Long todoId, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ti from TodoInstance ti where ti.id = :id")
    Optional<TodoInstance> findByIdForUpdate(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("delete from TodoInstance i where i.todo.id = :todoId")
    void deleteByTodoId(@Param("todoId") Long todoId);

    @Modifying(clearAutomatically = true)
    @Query("delete from TodoInstance i where i.todo.id = :todoId and i.date >= :fromDate")
    void deleteByTodoIdAndDateFrom(@Param("todoId") Long todoId, @Param("fromDate") LocalDate fromDate);

    @Query("""
        select ti
        from TodoInstance ti
        join fetch ti.todo t
        where t.user.id = :userId
          and ti.date = :date
        order by ti.sortOrder asc, t.id asc
        """)
    List<TodoInstance> findDailyInstances(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    @Query("""
		select i from TodoInstance i
		where i.todo.id in :todoIds
		  and i.date between :from and :to
		""")
    List<TodoInstance> findByTodoIdsAndDateRange(
            @Param("todoIds") List<Long> todoIds,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
