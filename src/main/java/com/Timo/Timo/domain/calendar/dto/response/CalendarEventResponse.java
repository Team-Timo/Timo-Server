package com.Timo.Timo.domain.calendar.dto.response;

public record CalendarEventResponse(
    String title
) {

  public static CalendarEventResponse of(String title) {
    return new CalendarEventResponse(title);
  }
}
