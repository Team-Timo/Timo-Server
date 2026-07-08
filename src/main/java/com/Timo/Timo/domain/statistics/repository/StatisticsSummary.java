package com.Timo.Timo.domain.statistics.repository;

public record StatisticsSummary(
	Long totalRecordSeconds,
	Integer timerRecordedDayCount,
	Integer activeDayCount,
	Integer completedTodoCount,
	Integer totalTodoCount
) {
}
