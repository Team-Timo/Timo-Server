package com.Timo.Timo.domain.ai.dto.response;

import com.Timo.Timo.domain.ai.enums.PatternBasis;

import io.swagger.v3.oas.annotations.media.Schema;

public record RecommendDurationResponse(
	@Schema(description = "추천 예상 소요 시간(분). 5분 단위", example = "45")
	Integer recommendedMinutes,

	@Schema(description = "추천 판단에 가장 크게 사용한 근거", example = "SAME_TAG")
	PatternBasis patternBasis,

	@Schema(description = "AI 또는 fallback 추천 설명", example = "비슷한 투두 기록을 기준으로 45분 정도를 추천해요.")
	String feedback
) {
}
