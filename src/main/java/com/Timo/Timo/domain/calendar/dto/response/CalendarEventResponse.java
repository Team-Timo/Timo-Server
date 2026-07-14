package com.Timo.Timo.domain.calendar.dto.response;

import com.Timo.Timo.domain.calendar.dto.CalendarEventItem;
import java.util.List;

public record CalendarEventResponse (
    List<CalendarEventItem> items
){}
