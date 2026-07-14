package com.Timo.Timo.domain.calendar.dto.response;

import com.Timo.Timo.domain.calendar.enums.CalendarFilter;
import java.time.LocalDate;
import java.util.List;

public record CalendarEventsViewResponse(
    String filter,
    LocalDate baseDate,
    List<CalendarDayResponse> days
) {

  public static CalendarEventsViewResponse of(CalendarFilter filter, LocalDate baseDate, List<CalendarDayResponse> days) {
    return new CalendarEventsViewResponse(filter.name(), baseDate, days);
  }
}
