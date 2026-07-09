package com.Timo.Timo.domain.timer.dto.response;

import com.Timo.Timo.domain.timer.entity.TimerRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record TimerActiveResponse (
    Long timerId,
    Long todoId,
    String todoTitle,
    String iconType,
    String status,
    Integer plannedSeconds,
    Integer extendedSeconds,
    Integer elapsedSeconds,
    Integer remainingSeconds,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(type = "string", example = "2026-07-09 10:14:19")
    LocalDateTime startedAt
){
  public static TimerActiveResponse of(TimerRecord timerRecord, int elapsedSeconds){
    int remainingSeconds = Math.max(
        0,
        timerRecord.getPlannedSeconds() + timerRecord.getExtendedSeconds() - elapsedSeconds
    );
    return new TimerActiveResponse(
        timerRecord.getId(),
        timerRecord.getTodo().getId(),
        timerRecord.getTodo().getTitle(),
        timerRecord.getTodo().getIcon() != null ? timerRecord.getTodo().getIcon().name() : null,
        timerRecord.getStatus().name(),
        timerRecord.getPlannedSeconds(),
        timerRecord.getExtendedSeconds(),
        elapsedSeconds,
        remainingSeconds,
        timerRecord.getStartedAt()
    );
  }
}
