package com.Timo.Timo.domain.calendar.dto.response;

import java.time.LocalDate;
import java.util.List;

public record CalendarDayResponse(
    LocalDate date,
    List<CalendarEventResponse> events
) {

  public static CalendarDayResponse of(LocalDate date, List<CalendarEventResponse> events) {
    return new CalendarDayResponse(date, events);
  }
}
