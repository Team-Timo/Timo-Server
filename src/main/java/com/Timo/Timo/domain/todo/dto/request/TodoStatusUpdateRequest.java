package com.Timo.Timo.domain.todo.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TodoStatusUpdateRequest(
		@JsonProperty("isCompleted")
		Boolean isCompleted,

		LocalDate date
) {
}
