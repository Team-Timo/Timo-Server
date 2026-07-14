package com.Timo.Timo.domain.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CalendarConnectResponse(
    boolean calendarConnected,
    String calendarEmail,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(example = "2026-06-22T14:00:00Z", type = "string", description = "UTC 기준 연동 시각")
    LocalDateTime connectedAt
) {}
