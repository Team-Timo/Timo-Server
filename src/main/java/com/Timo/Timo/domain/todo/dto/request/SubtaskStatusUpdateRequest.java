package com.Timo.Timo.domain.todo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record SubtaskStatusUpdateRequest(
		@NotNull
		@JsonProperty("isCompleted")
		Boolean isCompleted
) { }
