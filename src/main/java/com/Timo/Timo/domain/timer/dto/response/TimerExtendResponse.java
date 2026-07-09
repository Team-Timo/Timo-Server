package com.Timo.Timo.domain.timer.dto.response;

import com.Timo.Timo.domain.timer.entity.TimerRecord;

public record TimerExtendResponse (
    Long timerId,
    String status,
    Integer extendedSeconds,
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
