package com.Timo.Timo.domain.todo.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Timo.Timo.domain.todo.entity.SubtaskCompletion;

public interface SubtaskCompletionRepository extends JpaRepository<SubtaskCompletion, Long> {

	Optional<SubtaskCompletion> findByTodoInstance_IdAndSubtask_Id(Long todoInstanceId, Long subtaskId);

	List<SubtaskCompletion> findByTodoInstance_IdIn(Collection<Long> todoInstanceIds);

	@Modifying(clearAutomatically = true)
	@Query("delete from SubtaskCompletion sc where sc.todoInstance.todo.id = :todoId")
	void deleteByTodoId(@Param("todoId") Long todoId);

	@Modifying(clearAutomatically = true)
	@Query("delete from SubtaskCompletion sc where sc.subtask.id in :subtaskIds")
	void deleteBySubtaskIdIn(@Param("subtaskIds") Collection<Long> subtaskIds);
}
