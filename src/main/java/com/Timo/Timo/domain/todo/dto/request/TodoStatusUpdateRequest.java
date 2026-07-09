package com.Timo.Timo.domain.todo.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record TodoStatusUpdateRequest(
		@NotNull
		@JsonProperty("isCompleted")
		Boolean isCompleted,

		LocalDate date
) { }
