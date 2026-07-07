package com.Timo.Timo.domain.timer.dto.response;

import com.Timo.Timo.domain.timer.entity.TimerRecord;
import java.time.LocalDateTime;

public record TimerStartResponse(
    Long timerId,
    Long todoId,
    String status,
    Integer plannedMinutes,
    LocalDateTime startedAt
) {
  public static TimerStartResponse from(TimerRecord timerRecord) {
    return new TimerStartResponse(
        timerRecord.getId(),
        timerRecord.getTodo().getId(),
        timerRecord.getStatus().name(),
        timerRecord.getPlannedMinutes(),
        timerRecord.getStartedAt()
    );
  }
}
