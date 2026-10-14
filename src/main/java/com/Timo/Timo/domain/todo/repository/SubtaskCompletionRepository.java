package com.Timo.Timo.domain.todo.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.Timo.Timo.domain.todo.entity.SubtaskCompletion;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface SubtaskCompletionRepository extends JpaRepository<SubtaskCompletion, Long> {

	List<SubtaskCompletion> findByTodoInstance_IdIn(Collection<Long> todoInstanceIds);

	// 잠금 조회는 트랜잭션 스냅샷이 아닌 최신 커밋 값을 읽으므로, 먼저 처리된 요청의 변경을 덮어쓰지 않는다.
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints({
		@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"),
		@QueryHint(name = "jakarta.persistence.lock.scope", value = "NORMAL")
	})
	@Query("select sc from SubtaskCompletion sc where sc.todoInstance.id = :todoInstanceId")
	List<SubtaskCompletion> findAllByTodoInstanceIdForUpdate(@Param("todoInstanceId") Long todoInstanceId);

	@Modifying(clearAutomatically = true)
	@Query("delete from SubtaskCompletion sc where sc.todoInstance.id in "
			+ "(select ti.id from TodoInstance ti where ti.todo.id = :todoId)")
	void deleteByTodoId(@Param("todoId") Long todoId);

	@Modifying(clearAutomatically = true)
	@Query("delete from SubtaskCompletion sc where sc.subtask.id in :subtaskIds")
	void deleteBySubtaskIdIn(@Param("subtaskIds") Collection<Long> subtaskIds);
}
