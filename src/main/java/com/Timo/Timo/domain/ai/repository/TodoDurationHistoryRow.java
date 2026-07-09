package com.Timo.Timo.domain.ai.repository;

import java.time.LocalDateTime;

public record TodoDurationHistoryRow(
	String title,
	Integer actualSeconds,
	LocalDateTime recordedAt
) {}