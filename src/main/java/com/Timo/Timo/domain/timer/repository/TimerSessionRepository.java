package com.Timo.Timo.domain.timer.repository;

import com.Timo.Timo.domain.timer.entity.TimerSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimerSessionRepository extends JpaRepository<TimerSession, Long> {

  Optional<TimerSession> findByTimerRecordIdAndPausedAtIsNull(Long timerRecordId);

  List<TimerSession> findByTimerRecordId(Long timerRecordId);

  @Modifying(clearAutomatically = true)
  @Query("delete from TimerSession s where s.timerRecord.todo.id = :todoId")
  void deleteByTodoId(@Param("todoId") Long todoId);

  @Query("""
      select ts
      from TimerSession ts
      join fetch ts.timerRecord tr
      join fetch tr.todo
      where tr.user.id = :userId
        and ts.startedAt < :toExclusive
        and coalesce(ts.pausedAt, :nowUtc) > :fromInclusive
      order by ts.startedAt asc, ts.id asc
      """)
  List<TimerSession> findTimeBoxSessions(
      @Param("userId") Long userId,
      @Param("fromInclusive") LocalDateTime fromInclusive,
      @Param("toExclusive") LocalDateTime toExclusive,
      @Param("nowUtc") LocalDateTime nowUtc
  );
}
