package com.Timo.Timo.domain.ai.repository;

import java.time.LocalDate;

public record TodoDurationHistory(
	String title,
	Long tagId,
	Integer actualSeconds,
	LocalDate date
) {
}
