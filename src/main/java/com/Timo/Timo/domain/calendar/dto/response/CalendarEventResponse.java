package com.Timo.Timo.domain.calendar.dto.response;

import com.Timo.Timo.domain.todo.enums.CalendarSource;

public record CalendarEventResponse(
    String title,
    CalendarSource calendarSource
) {

  public static CalendarEventResponse of(String title, CalendarSource calendarSource) {
    return new CalendarEventResponse(title, calendarSource);
  }
}
