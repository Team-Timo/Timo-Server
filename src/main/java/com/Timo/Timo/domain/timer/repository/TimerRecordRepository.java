package com.Timo.Timo.domain.timer.repository;

import com.Timo.Timo.domain.timer.entity.TimerRecord;
import com.Timo.Timo.domain.timer.enums.TimerStatus;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface TimerRecordRepository extends JpaRepository<TimerRecord, Long> {

  Optional<TimerRecord> findByUserIdAndStatusIn(Long userId, List<TimerStatus> statuses);

  boolean existsByTodo_IdAndStatusIn(Long todoId, List<TimerStatus> statuses);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from TimerRecord t where t.id = :id")
  Optional<TimerRecord> findByIdForUpdate(@Param("id") Long id);
}
