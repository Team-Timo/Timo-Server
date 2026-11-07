package com.Timo.Timo.domain.timer.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TimerStatisticsRecord {
	Long getTodoId();
	LocalDate getTargetDate();
	LocalDateTime getStartedAt();
	Integer getActualSeconds();
}
