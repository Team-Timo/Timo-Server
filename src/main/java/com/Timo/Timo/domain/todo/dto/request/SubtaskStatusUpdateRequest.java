package com.Timo.Timo.domain.todo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SubtaskStatusUpdateRequest(
		@JsonProperty("isCompleted")
		Boolean isCompleted
) { }
