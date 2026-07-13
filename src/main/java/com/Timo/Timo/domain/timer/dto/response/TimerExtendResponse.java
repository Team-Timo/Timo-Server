package com.Timo.Timo.domain.timer.dto.response;

import com.Timo.Timo.domain.timer.entity.TimerRecord;
import io.swagger.v3.oas.annotations.media.Schema;

public record TimerExtendResponse (
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long timerId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String status,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer extendedSeconds,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer remainingSeconds
){
  public static TimerExtendResponse of(TimerRecord timerRecord, int remainingSeconds){
    return new TimerExtendResponse(
        timerRecord.getId(),
        timerRecord.getStatus().name(),
        timerRecord.getExtendedSeconds(),
        remainingSeconds
    );
  }
}
