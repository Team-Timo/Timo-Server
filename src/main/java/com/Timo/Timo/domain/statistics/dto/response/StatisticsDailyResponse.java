package com.Timo.Timo.domain.statistics.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

public record StatisticsDailyResponse(
	@Schema(description = "조회 날짜", example = "2026-06-28", requiredMode = Schema.RequiredMode.REQUIRED)
	LocalDate date,

	@Schema(description = "해당 날짜에 완료된 투두의 총 기록시간(분). todos의 actualTimeMinutes 합계와 같음", example = "260", requiredMode = Schema.RequiredMode.REQUIRED)
	Long totalRecordMinutes,

	@ArraySchema(
		schema = @Schema(description = "해당 날짜에 계획된 투두 중 완료된 투두 목록"),
		arraySchema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
	)
	List<DailyTodoResponse> todos
) {
	public record DailyTodoResponse(
		@Schema(description = "투두 ID", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
		Long todoId,

		@Schema(description = "투두명", example = "와이어프레임 제작", requiredMode = Schema.RequiredMode.REQUIRED)
		String title,

		@Schema(description = "해당 날짜에 계획된 이 투두의 타이머 기록 합계(분, 초는 반올림), 기록이 없으면 0", example = "90", requiredMode = Schema.RequiredMode.REQUIRED)
		Long actualTimeMinutes,

		@Schema(description = "예상 소요 시간(분)", example = "60")
		Long estimatedTimeMinutes,

		@Schema(description = "투두에 설정된 태그", nullable = true)
		TagResponse tag
	) {}

	public record TagResponse(
		@Schema(description = "태그명", example = "과제", requiredMode = Schema.RequiredMode.REQUIRED)
		String name
	) {}
}
