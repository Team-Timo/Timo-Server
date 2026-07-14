package com.Timo.Timo.domain.statistics.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

public record StatisticsCalendarResponse(
	@Schema(description = "조회한 연월", example = "2026-06", requiredMode = Schema.RequiredMode.REQUIRED)
	String yearMonth,

	@Schema(description = "UTC 기준 오늘 날짜", example = "2026-06-28", requiredMode = Schema.RequiredMode.REQUIRED)
	LocalDate today,

	@ArraySchema(
		schema = @Schema(description = "조회 월의 날짜별 완료 현황"),
		arraySchema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
	)
	List<DayCompletionResponse> days
) {

	public record DayCompletionResponse(
		@Schema(description = "통계 날짜", example = "2026-06-01", requiredMode = Schema.RequiredMode.REQUIRED)
		LocalDate date,

		@Schema(description = "투두 완료율. 투두가 없으면 0", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
		Integer completionRate
	) { }
}
