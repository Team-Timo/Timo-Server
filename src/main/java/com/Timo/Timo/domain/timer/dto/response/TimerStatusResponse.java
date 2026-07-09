package com.Timo.Timo.domain.timer.dto.response;

import com.Timo.Timo.domain.timer.entity.TimerRecord;

public record TimerStatusResponse (
    Long timerId,
    String status,
    Integer elapsedSeconds,
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
