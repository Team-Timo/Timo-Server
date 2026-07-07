package com.Timo.Timo.domain.ai.dto.response;

import com.Timo.Timo.domain.ai.enums.PatternBasis;

public record GeminiDurationRecommendation(
	Integer recommendedMinutes,
	PatternBasis patternBasis,
	String feedback
) {
}
