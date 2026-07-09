package com.Timo.Timo.domain.home.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.home.dto.response.HomeResponse;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.DayResponse;
import com.Timo.Timo.domain.home.dto.response.TodayResponse;
import com.Timo.Timo.domain.home.enums.HomeFilter;
import com.Timo.Timo.domain.home.exception.HomeErrorCode;
import com.Timo.Timo.domain.home.service.HomeTodoReader.LoadedTodos;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.enums.Language;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

	private final UserRepository userRepository;
	private final HomeTodoReader homeTodoReader;
	private final HolidayChecker holidayChecker;

	public HomeResponse getHome(Long userId, String filterValue, String baseDateValue) {
		User user = getUser(userId);
		ZoneId userZone = ZoneId.of(user.getZoneId());
		Language language = user.getLanguage();

		HomeFilter filter = HomeFilter.from(filterValue);
		LocalDate baseDate = parseBaseDate(baseDateValue, userZone);
		LocalDate from = filter.rangeStart(baseDate);
		LocalDate to = filter.rangeEnd(baseDate);
		LocalDate today = LocalDate.now(userZone);

		LoadedTodos loaded = homeTodoReader.load(userId, from, to);

		List<DayResponse> days = from.datesUntil(to.plusDays(1))
				.map(date -> DayResponse.of(
						date,
						holidayChecker.isHoliday(date, language),
						date.equals(today),
						homeTodoReader.sortedTodosOn(loaded, date)
				))
				.toList();

		return new HomeResponse(filter, baseDate, days);
	}

	public TodayResponse getToday(Long userId) {
		User user = getUser(userId);
		LocalDate today = LocalDate.now(ZoneId.of(user.getZoneId()));

		LoadedTodos loaded = homeTodoReader.load(userId, today, today);

		return TodayResponse.of(today, homeTodoReader.sortedTodosOn(loaded, today));
	}

	private User getUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
	}

	private LocalDate parseBaseDate(String baseDateValue, ZoneId userZone) {
		if (baseDateValue == null || baseDateValue.isBlank()) {
			return LocalDate.now(userZone);
		}

		try {
			return LocalDate.parse(baseDateValue);
		} catch (DateTimeParseException exception) {
			throw new CustomException(HomeErrorCode.INVALID_DATE);
		}
	}
}
