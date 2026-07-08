package com.Timo.Timo.domain.statistics.repository;

public record StatisticsDailyTodo(
	Long todoId,
	String title,
	Long actualSeconds,
	Integer estimatedSeconds,
	Long tagId
) {
}
