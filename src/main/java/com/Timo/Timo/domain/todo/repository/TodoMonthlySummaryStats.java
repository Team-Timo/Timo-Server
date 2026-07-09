package com.Timo.Timo.domain.todo.repository;

public interface TodoMonthlySummaryStats {
	Long getActiveDayCount();
	Long getCompletedTodoCount();
	Long getTotalTodoCount();
}