package com.Timo.Timo.domain.timer.repository;

import com.Timo.Timo.domain.timer.entity.TimerRecord;
import com.Timo.Timo.domain.timer.enums.TimerStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimerRecordRepository extends JpaRepository<TimerRecord, Long> {

  Optional<TimerRecord> findByUserIdAndStatusIn(Long userId, List<TimerStatus> statuses);

  boolean existsByTodo_IdAndStatusIn(Long todoId, List<TimerStatus> statuses);

  @Modifying(clearAutomatically = true)
  @Query("delete from TimerRecord r where r.todo.id = :todoId")
  void deleteByTodoId(@Param("todoId") Long todoId);
}
