package com.Timo.Timo.domain.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CalendarConnectResponse(
    boolean calendarConnected,
    String calendarEmail,

    @Schema(example = "2026-06-22 14:00:00", type = "string", description = "사용자 시간대(zoneId) 기준 연동 시각")
    String connectedAt
) {}
