package com.Timo.Timo.domain.timer.dto.response;

import com.Timo.Timo.domain.timer.entity.TimerRecord;
import io.swagger.v3.oas.annotations.media.Schema;

public record TimerFinishResponse(
    Long timerId,
    Long todoId,
    String status,
    Integer plannedSeconds,
    Integer actualSeconds,
    @Schema(description = "AI 피드백 문구. AI 호출 실패 시 null", nullable = true)
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