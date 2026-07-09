package com.Timo.Timo.domain.statistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record StatisticsSummaryResponse(
	@Schema(description = "해당 월의 전체 타이머 기록 시간(분)", example = "283200")
	Long totalRecordMinutes,

	@Schema(description = "1개 이상의 투두를 생성한 날짜 수", example = "28")
	Integer activeDayCount,

	@Schema(description = "타이머를 실행한 날짜 기준 일평균 기록 시간(분)", example = "204")
	Long averageRecordedMinutes,

	@Schema(description = "해당 월에 완료한 투두 수", example = "80")
	Integer completedTodoCount,

	@Schema(description = "해당 월에 작성된 전체 투두 수", example = "100")
	Integer totalTodoCount
) {
}
