package com.Timo.Timo.domain.timebox.support;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.timebox.exception.TimeBoxErrorCode;
import com.Timo.Timo.global.exception.CustomException;

@Component
public class TimeBoxDateParser {

	private static final String DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
	private static final DateTimeFormatter DATE_FORMATTER =
		DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

	public LocalDate parse(String date) {
		if (date == null || date.isBlank()) {
			throw new CustomException(TimeBoxErrorCode.DATE_REQUIRED);
		}
		if (!date.matches(DATE_PATTERN)) {
			throw new CustomException(TimeBoxErrorCode.INVALID_DATE_FORMAT);
		}

		try {
			return LocalDate.parse(date, DATE_FORMATTER);
		} catch (DateTimeParseException exception) {
			throw new CustomException(TimeBoxErrorCode.INVALID_DATE);
		}
	}
}