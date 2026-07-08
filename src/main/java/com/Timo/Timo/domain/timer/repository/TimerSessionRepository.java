package com.Timo.Timo.domain.timer.repository;

import com.Timo.Timo.domain.timer.entity.TimerSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimerSessionRepository extends JpaRepository<TimerSession, Long> {

  Optional<TimerSession> findByTimerRecordIdAndPausedAtIsNull(Long timerRecordId);

  @Modifying(clearAutomatically = true)
  @Query("delete from TimerSession s where s.timerRecord.todo.id = :todoId")
  void deleteByTodoId(@Param("todoId") Long todoId);
}
