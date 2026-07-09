package com.Timo.Timo.domain.todo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Timo.Timo.domain.todo.entity.TodoInstance;

public interface TodoInstanceRepository extends JpaRepository<TodoInstance, Long> {

    Optional<TodoInstance> findByTodo_IdAndDate(Long todoId, LocalDate date);

    @Modifying(clearAutomatically = true)
    @Query("delete from TodoInstance i where i.todo.id = :todoId")
    void deleteByTodoId(@Param("todoId") Long todoId);

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