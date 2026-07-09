package com.Timo.Timo.domain.timer.dto.response;

import com.Timo.Timo.domain.timer.entity.TimerRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record TimerStartResponse(
    Long timerId,
    Long todoId,
    String status,
    Integer plannedSeconds,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(example = "2026-07-06 17:51:50", type = "string")
    LocalDateTime startedAt
) {
  public static TimerStartResponse from(TimerRecord timerRecord) {
    return new TimerStartResponse(
        timerRecord.getId(),
        timerRecord.getTodo().getId(),
        timerRecord.getStatus().name(),
        timerRecord.getPlannedSeconds(),
        timerRecord.getStartedAt()
    );
  }
}
