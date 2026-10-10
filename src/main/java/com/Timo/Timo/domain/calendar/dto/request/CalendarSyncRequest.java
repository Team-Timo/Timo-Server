package com.Timo.Timo.domain.calendar.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record CalendarSyncRequest(
    @Schema(description = "동기화 범위 필터 (DEFAULT: 기준일 ±7일, WEEK: 기준일부터 7일), 미입력 시 DEFAULT", example = "DEFAULT")
    String filter,

    @Schema(description = "기준 날짜 (yyyy-MM-dd), 미입력 시 오늘", example = "2026-10-09")
    String baseDate
) {}
