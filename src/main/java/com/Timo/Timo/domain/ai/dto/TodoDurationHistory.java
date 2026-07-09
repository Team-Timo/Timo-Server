package com.Timo.Timo.domain.ai.dto;

import java.time.LocalDate;

public record TodoDurationHistory(
	String title,
	Integer actualSeconds,
	LocalDate date
) {}
