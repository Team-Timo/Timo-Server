package com.Timo.Timo.domain.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CalendarConnectResponse(
    boolean calendarConnected,
    String calendarEmail,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(example = "2026-07-06 17:51:50", type = "string")
    LocalDateTime connectedAt
) {}
