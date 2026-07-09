package com.Timo.Timo.domain.timer.dto.response;

import com.Timo.Timo.domain.timer.entity.TimerRecord;

public record TimerFinishResponse(
    Long timerId,
    Long todoId,
    String status,
    Integer plannedSeconds,
    Integer actualSeconds,
    String aiFeedback
) {
  public static TimerFinishResponse of(TimerRecord timerRecord) {
    return new TimerFinishResponse(
        timerRecord.getId(),
        timerRecord.getTodo().getId(),
        timerRecord.getStatus().name(),
        timerRecord.getPlannedSeconds(),
        timerRecord.getActualSeconds(),
        timerRecord.getAiFeedback()
    );
  }
}
