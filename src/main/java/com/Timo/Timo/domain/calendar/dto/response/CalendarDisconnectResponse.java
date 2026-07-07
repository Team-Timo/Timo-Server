package com.Timo.Timo.domain.calendar.dto.response;

import lombok.Builder;

@Builder
public record CalendarDisconnectResponse(
    boolean calendarConnected
) {

}
