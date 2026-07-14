package com.Timo.Timo.domain.calendar.dto;

public record CalendarEventItem (
    String id,
    String summary,
    GoogleEventDateTime start,
    GoogleEventDateTime end
){}
