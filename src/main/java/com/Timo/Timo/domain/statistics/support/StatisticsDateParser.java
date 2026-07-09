package com.Timo.Timo.domain.statistics.support;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.statistics.exception.StatisticsErrorCode;
import com.Timo.Timo.global.exception.CustomException;

@Component
public class StatisticsDateParser {

	private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
	private static final String YEAR_MONTH_PATTERN = "^\\d{4}-\\d{2}$";

	public YearMonth parseYearMonth(String yearMonth) {
		if (yearMonth == null || yearMonth.isBlank()) {
			throw new CustomException(StatisticsErrorCode.YEAR_MONTH_REQUIRED);
		}

		if (!yearMonth.matches(YEAR_MONTH_PATTERN)) {
			throw new CustomException(StatisticsErrorCode.INVALID_YEAR_MONTH_FORMAT);
		}

		try {
			return YearMonth.parse(yearMonth, YEAR_MONTH_FORMATTER);
		} catch (DateTimeParseException exception) {
			throw new CustomException(StatisticsErrorCode.INVALID_YEAR_MONTH);
		}
	}
}
