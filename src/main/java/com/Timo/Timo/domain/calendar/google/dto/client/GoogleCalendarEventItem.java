package com.Timo.Timo.domain.calendar.google.dto.client;

import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;

public record GoogleCalendarEventItem (
    String id,
    String summary,
    GoogleEventDateTime start,
    GoogleEventDateTime end
){

  public ExternalCalendarEvent toExternalEvent() {
    return new ExternalCalendarEvent(
        id,
        summary,
        start != null ? start.toExternalEventDateTime() : null,
        end != null ? end.toExternalEventDateTime() : null
    );
  }
}
