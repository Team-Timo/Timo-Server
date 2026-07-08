package com.Timo.Timo.domain.timer.repository;

import com.Timo.Timo.domain.timer.entity.TimerRecord;
import com.Timo.Timo.domain.timer.enums.TimerStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimerRecordRepository extends JpaRepository<TimerRecord, Long> {

  Optional<TimerRecord> findByUserIdAndStatusIn(Long userId, List<TimerStatus> statuses);
}
