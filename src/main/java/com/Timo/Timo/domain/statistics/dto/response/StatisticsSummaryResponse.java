package com.Timo.Timo.domain.statistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record StatisticsSummaryResponse(
	@Schema(description = "해당 월에 완료된 투두의 전체 기록 시간(분). 일별 기록시간의 합계와 같음", example = "283200", requiredMode = Schema.RequiredMode.REQUIRED)
	Long totalRecordMinutes,

	@Schema(description = "타이머를 실행했거나 투두를 완료한 날짜 수. 투두가 계획된 날짜 기준", example = "28", requiredMode = Schema.RequiredMode.REQUIRED)
	Integer activeDayCount,

	@Schema(description = "기록시간이 1분 이상인 날짜 기준 일평균 기록 시간(분)", example = "204", requiredMode = Schema.RequiredMode.REQUIRED)
	Long averageRecordedMinutes,

	@Schema(description = "해당 월에 완료한 투두 수", example = "80", requiredMode = Schema.RequiredMode.REQUIRED)
	Integer completedTodoCount,

	@Schema(description = "해당 월에 작성된 전체 투두 수", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
	Integer totalTodoCount
) {}