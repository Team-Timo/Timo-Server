package com.Timo.Timo.domain.timer.repository;

import com.Timo.Timo.domain.timer.entity.TimerRecord;
import com.Timo.Timo.domain.timer.enums.TimerStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimerRecordRepository extends JpaRepository<TimerRecord, Long> {

  Optional<TimerRecord> findByUserIdAndStatusIn(Long userId, List<TimerStatus> statuses);

  boolean existsByTodo_IdAndStatusIn(Long todoId, List<TimerStatus> statuses);

  boolean existsByTodo_IdAndTargetDateAndStatusIn(
      Long todoId, LocalDate targetDate, List<TimerStatus> statuses);

  @Modifying(clearAutomatically = true)
  @Query("delete from TimerRecord r where r.todo.id = :todoId")
  void deleteByTodoId(@Param("todoId") Long todoId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from TimerRecord t where t.id = :id")
  Optional<TimerRecord> findByIdForUpdate(@Param("id") Long id);

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

  @Query("""
      select coalesce(sum(tr.actualSeconds), 0)
      from TimerRecord tr
      where tr.user.id = :userId
        and tr.actualSeconds is not null
        and coalesce(tr.endedAt, tr.startedAt) >= :fromInclusive
        and coalesce(tr.endedAt, tr.startedAt) < :toExclusive
      """)
  Long sumActualSeconds(
      @Param("userId") Long userId,
      @Param("fromInclusive") LocalDateTime fromInclusive,
      @Param("toExclusive") LocalDateTime toExclusive
  );

  @Query("""
      select
        tr.todo.id as todoId,
        coalesce(sum(tr.actualSeconds), 0) as actualSeconds
      from TimerRecord tr
      where tr.user.id = :userId
        and tr.actualSeconds is not null
        and coalesce(tr.endedAt, tr.startedAt) >= :fromInclusive
        and coalesce(tr.endedAt, tr.startedAt) < :toExclusive
      group by tr.todo.id
      """)
  List<TimerDailyTodoStats> findDailyTodoStats(
      @Param("userId") Long userId,
      @Param("fromInclusive") LocalDateTime fromInclusive,
      @Param("toExclusive") LocalDateTime toExclusive
  );

  @Query("""
      select coalesce(tr.endedAt, tr.startedAt)
      from TimerRecord tr
      where tr.user.id = :userId
        and tr.actualSeconds is not null
        and coalesce(tr.endedAt, tr.startedAt) >= :fromInclusive
        and coalesce(tr.endedAt, tr.startedAt) < :toExclusive
      """)
  List<LocalDateTime> findMonthlyRecordedAtTimes(
      @Param("userId") Long userId,
      @Param("fromInclusive") LocalDateTime fromInclusive,
      @Param("toExclusive") LocalDateTime toExclusive
  );
}
