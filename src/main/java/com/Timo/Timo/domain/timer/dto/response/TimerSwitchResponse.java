package com.Timo.Timo.domain.timer.dto.response;

import com.Timo.Timo.domain.timer.entity.TimerRecord;
import io.swagger.v3.oas.annotations.media.Schema;

public record TimerSwitchResponse(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Stopped stopped,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    TimerStartResponse started
) {

  public static TimerSwitchResponse of(TimerRecord stopped, TimerStartResponse started) {
    return new TimerSwitchResponse(Stopped.from(stopped), started);
  }

  public record Stopped(
      @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
      Long timerId,
      @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
      Long todoId,
      @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
      String status,
      @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
      Integer plannedSeconds,
      @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
      Integer actualSeconds
  ) {

    public static Stopped from(TimerRecord timerRecord) {
      return new Stopped(
          timerRecord.getId(),
          timerRecord.getTodo().getId(),
          timerRecord.getStatus().name(),
          timerRecord.getPlannedSeconds(),
          timerRecord.getActualSeconds()
      );
    }
  }
}
