package com.Timo.Timo.domain.timer.dto.response;

import com.Timo.Timo.domain.timer.entity.TimerRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TimerActiveResponse (
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long timerId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long todoId,
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(type = "string", example = "2026-07-15", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate date,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String todoTitle,
    String iconType,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String status,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer plannedSeconds,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer extendedSeconds,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer elapsedSeconds,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer remainingSeconds,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(type = "string", example = "2026-07-09 10:14:19", requiredMode = Schema.RequiredMode.REQUIRED)
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
        timerRecord.getTimerDate(),
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
