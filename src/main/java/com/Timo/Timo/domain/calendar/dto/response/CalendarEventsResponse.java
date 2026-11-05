package com.Timo.Timo.domain.calendar.dto.response;

import com.Timo.Timo.domain.calendar.enums.CalendarFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record CalendarEventsResponse(
    String filter,
    LocalDate baseDate,
    List<CalendarDayResponse> days,

    @Schema(description = "조회에 실패한 캘린더 목록 (모두 성공하면 빈 배열)", requiredMode = Schema.RequiredMode.REQUIRED)
    List<CalendarSourceFailureResponse> failedSources
) {

  public static CalendarEventsResponse of(
      CalendarFilter filter,
      LocalDate baseDate,
      List<CalendarDayResponse> days,
      List<CalendarSourceFailureResponse> failedSources
  ) {
    return new CalendarEventsResponse(filter.name(), baseDate, days, failedSources);
  }
}
