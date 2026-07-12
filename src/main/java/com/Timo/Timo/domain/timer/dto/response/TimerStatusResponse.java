package com.Timo.Timo.domain.timer.dto.response;

import com.Timo.Timo.domain.timer.entity.TimerRecord;
import io.swagger.v3.oas.annotations.media.Schema;

public record TimerStatusResponse (
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long timerId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String status,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer elapsedSeconds,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer remainingSeconds
){
  public static TimerStatusResponse of(TimerRecord timerRecord, int elapsedSeconds){

    int remainingSeconds = Math.max(
        0,
        timerRecord.getPlannedSeconds() + timerRecord.getExtendedSeconds() - elapsedSeconds
    );

    return new TimerStatusResponse(
        timerRecord.getId(),
        timerRecord.getStatus().name(),
        elapsedSeconds,
        remainingSeconds
    );
  }
}
