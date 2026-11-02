package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import java.time.Instant;
import java.util.List;

public interface CalendarEventProvider {

  CalendarSource source();

  boolean isConnected(Long userId);

  List<ExternalCalendarEvent> fetchEvents(Long userId, Instant timeMin, Instant timeMax);
}
