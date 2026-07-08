package com.Timo.Timo.domain.timer.repository;

import com.Timo.Timo.domain.timer.entity.TimerRecord;
import com.Timo.Timo.domain.timer.enums.TimerStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimerRecordRepository extends JpaRepository<TimerRecord, Long> {

  Optional<TimerRecord> findByUserIdAndStatusIn(Long userId, List<TimerStatus> statuses);

  boolean existsByTodo_IdAndStatusIn(Long todoId, List<TimerStatus> statuses);

  @Query("""
      select
        coalesce(sum(tr.actualSeconds), 0) as totalRecordSeconds,
        count(distinct function('date', coalesce(tr.endedAt, tr.startedAt))) as timerRecordedDayCount
      from TimerRecord tr
      where tr.user.id = :userId
        and tr.actualSeconds is not null
        and coalesce(tr.endedAt, tr.startedAt) >= :fromInclusive
        and coalesce(tr.endedAt, tr.startedAt) < :toExclusive
      """)
  TimerMonthlyRecordStats findMonthlyRecordStats(
      @Param("userId") Long userId,
      @Param("fromInclusive") LocalDateTime fromInclusive,
      @Param("toExclusive") LocalDateTime toExclusive
  );
}
