package com.Timo.Timo.domain.calendar.dto.response;

import com.Timo.Timo.domain.todo.enums.CalendarSource;
import com.Timo.Timo.global.exception.code.BaseErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record CalendarSourceFailureResponse(
    @Schema(description = "처리에 실패한 캘린더 (GOOGLE/APPLE)", example = "GOOGLE")
    CalendarSource calendarSource,

    @Schema(description = "실패 원인 에러 코드", example = "GOOGLE_CALENDAR_401")
    String errorCode,

    @Schema(description = "실패 원인 메시지", example = "구글 캘린더 인증에 실패했습니다.")
    String message
) {

  public static CalendarSourceFailureResponse of(CalendarSource calendarSource, BaseErrorCode errorCode) {
    return new CalendarSourceFailureResponse(calendarSource, errorCode.getCode(), errorCode.getMessage());
  }
}
