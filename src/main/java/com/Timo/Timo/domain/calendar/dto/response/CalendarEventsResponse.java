package com.Timo.Timo.domain.calendar.dto.response;

import com.Timo.Timo.domain.calendar.enums.CalendarFilter;
import java.time.LocalDate;
import java.util.List;

public record CalendarEventsResponse(
    String filter,
    LocalDate baseDate,
    List<CalendarDayResponse> days
) {

  public static CalendarEventsResponse of(CalendarFilter filter, LocalDate baseDate, List<CalendarDayResponse> days) {
    return new CalendarEventsResponse(filter.name(), baseDate, days);
  }
}