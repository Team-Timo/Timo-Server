package com.Timo.Timo.domain.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecommendDurationRequest(
	@NotBlank
	@Size(max = 30)
	@Schema(description = "추천받을 투두명", example = "알고리즘 문제 풀기")
	String title,

	@Schema(description = "투두 태그 ID", example = "1", nullable = true)
	Long tagId
) {
}
