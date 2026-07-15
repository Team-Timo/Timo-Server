package com.Timo.Timo.domain.calendar.dto.client;

public record CalendarEventItem (
    String id,
    String summary,
    GoogleEventDateTime start,
    GoogleEventDateTime end
){}
