package com.Timo.Timo.domain.home.enums;

import java.time.LocalDate;

import com.Timo.Timo.domain.home.exception.HomeErrorCode;
import com.Timo.Timo.global.exception.CustomException;

public enum HomeFilter {
	DEFAULT {
		@Override
		public LocalDate rangeStart(LocalDate baseDate) {
			return baseDate.minusDays(7);
		}

		@Override
		public LocalDate rangeEnd(LocalDate baseDate) {
			return baseDate.plusDays(7);
		}
	},
	WEEK {
		@Override
		public LocalDate rangeStart(LocalDate baseDate) {
			return baseDate;
		}

		@Override
		public LocalDate rangeEnd(LocalDate baseDate) {
			return baseDate.plusDays(6);
		}
	};

	public abstract LocalDate rangeStart(LocalDate baseDate);

	public abstract LocalDate rangeEnd(LocalDate baseDate);

	public static HomeFilter from(String value) {
		if (value == null || value.isBlank()) {
			return DEFAULT;
		}

		try {
			return valueOf(value);
		} catch (IllegalArgumentException exception) {
			throw new CustomException(HomeErrorCode.INVALID_FILTER_OR_DATE);
		}
	}
}
