package com.Timo.Timo.domain.timer.repository;

import com.Timo.Timo.domain.timer.entity.TimerSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimerSessionRepository extends JpaRepository<TimerSession, Long> {

  Optional<TimerSession> findByTimerRecordIdAndPausedAtIsNull(Long timerRecordId);
}
