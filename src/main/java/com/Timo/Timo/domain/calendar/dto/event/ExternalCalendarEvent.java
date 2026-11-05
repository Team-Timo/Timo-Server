package com.Timo.Timo.domain.calendar.dto.event;

public record ExternalCalendarEvent(
    String eventId,
    String title,
    ExternalEventDateTime start,
    ExternalEventDateTime end
) {}
