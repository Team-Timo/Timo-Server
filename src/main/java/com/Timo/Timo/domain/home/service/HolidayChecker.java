package com.Timo.Timo.domain.home.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.user.enums.Language;

@Component
public class HolidayChecker {

	private static final Set<MonthDay> KOREAN_FIXED_HOLIDAYS = Set.of(
			MonthDay.of(1, 1),
			MonthDay.of(3, 1),
			MonthDay.of(5, 5),
			MonthDay.of(6, 6),
			MonthDay.of(8, 15),
			MonthDay.of(10, 3),
			MonthDay.of(10, 9),
			MonthDay.of(12, 25)
	);

	/**
	 * 언어(지역)별 양력 고정 공휴일. 음력 공휴일(설날·추석 등)은 별도 계산이 필요해 아직 포함하지 않는다.
	 * 신뢰할 만한 데이터가 없는 언어는 빈 집합으로 두고 일요일만 공휴일로 처리한다.
	 */
	private static final Map<Language, Set<MonthDay>> FIXED_HOLIDAYS_BY_LANGUAGE = Map.of(
			Language.KO, KOREAN_FIXED_HOLIDAYS,
			Language.EN, Set.of()
	);

	public boolean isHoliday(LocalDate date, Language language) {
		if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
			return true;
		}

		Set<MonthDay> fixedHolidays = FIXED_HOLIDAYS_BY_LANGUAGE.getOrDefault(language, Set.of());
		return fixedHolidays.contains(MonthDay.from(date));
	}
}
