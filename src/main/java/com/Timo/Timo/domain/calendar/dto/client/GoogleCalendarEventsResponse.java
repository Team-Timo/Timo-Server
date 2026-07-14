package com.Timo.Timo.domain.calendar.dto.client;

import java.util.List;

public record GoogleCalendarEventsResponse(
    List<CalendarEventItem> items
) {}
