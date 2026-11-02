package com.Timo.Timo.domain.calendar.google.dto.client;

import java.util.List;

public record GoogleCalendarEventsResponse(
    List<GoogleCalendarEventItem> items,
    String nextPageToken
) {}
