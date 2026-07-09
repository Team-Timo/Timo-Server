package com.Timo.Timo.domain.ai.dto;

public record TodoFeedbackSource(
	String title,
	Long tagId,
	String tagName,
	Integer estimatedSeconds,
	Integer actualSeconds
) {}