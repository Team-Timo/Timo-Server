package com.Timo.Timo.domain.todo.dto.request;

import java.time.LocalDate;

public record TodoReorderRequest(
		Integer newIndex,

		LocalDate date
) { }
