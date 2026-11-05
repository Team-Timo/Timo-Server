package com.Timo.Timo.domain.calendar.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CalendarSyncResponse(
    @Schema(description = "동기화에 실패한 캘린더 목록 (모두 성공하면 빈 배열)", requiredMode = Schema.RequiredMode.REQUIRED)
    List<CalendarSourceFailureResponse> failedSources
) {

  public static CalendarSyncResponse of(List<CalendarSourceFailureResponse> failedSources) {
    return new CalendarSyncResponse(failedSources);
  }
}
