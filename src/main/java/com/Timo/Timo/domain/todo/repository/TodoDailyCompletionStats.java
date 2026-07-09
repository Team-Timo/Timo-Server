package com.Timo.Timo.domain.todo.repository;

import java.time.LocalDate;

public interface TodoDailyCompletionStats {
	LocalDate getDate();
	Long getTotalCount();
	Long getCompletedCount();
}