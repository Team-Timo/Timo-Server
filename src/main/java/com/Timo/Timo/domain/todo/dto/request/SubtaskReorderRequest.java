package com.Timo.Timo.domain.todo.dto.request;

import java.time.LocalDate;

public record SubtaskReorderRequest(
		Integer newIndex,

		LocalDate date
) { }
