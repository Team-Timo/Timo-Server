package com.Timo.Timo.domain.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateTodoFeedbackResponse(
	@Schema(description = "AI 투두 수행 피드백")
	String feedback
) {}