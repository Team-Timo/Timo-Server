package com.Timo.Timo.domain.calendar.dto.client;

import com.Timo.Timo.domain.calendar.dto.CalendarEventItem;
import java.util.List;

public record GoogleCalendarEventsResponse(
    List<CalendarEventItem> items
) {}
