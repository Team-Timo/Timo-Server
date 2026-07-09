package com.Timo.Timo.domain.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTodoFeedbackRequest(
	@NotNull
	@Positive
	@Schema(description = "피드백을 생성할 투두 ID")
	Long todoId
) {}