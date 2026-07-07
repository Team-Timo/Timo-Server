package com.Timo.Timo.domain.calendar.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CalendarConnectResponse(
    boolean calendarConnected,
    String calendarEmail,
    LocalDateTime connectedAt
) {}
