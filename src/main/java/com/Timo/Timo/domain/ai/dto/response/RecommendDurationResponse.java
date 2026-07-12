package com.Timo.Timo.domain.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record RecommendDurationResponse(
	@Schema(description = "추천 예상 소요 시간(분)", example = "45", requiredMode = Schema.RequiredMode.REQUIRED)
	Integer recommendedMinutes
) {}