package com.Timo.Timo.domain.statistics.controller;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Timo.Timo.domain.statistics.docs.StatisticsCalendarDocs;
import com.Timo.Timo.domain.statistics.dto.response.StatisticsCalendarResponse;
import com.Timo.Timo.domain.statistics.exception.StatisticsErrorCode;
import com.Timo.Timo.domain.statistics.exception.StatisticsSuccessCode;
import com.Timo.Timo.domain.statistics.service.StatisticsService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.CustomException;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "통계 API")
public class StatisticsController implements StatisticsCalendarDocs {

	private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
	private static final String YEAR_MONTH_PATTERN = "^\\d{4}-\\d{2}$";

	private final StatisticsService statisticsService;

	@Override
	@GetMapping("/calendar")
	public ResponseEntity<BaseResponse<StatisticsCalendarResponse>> getCalendar(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(required = false) String yearMonth
	) {
		YearMonth parsedYearMonth = parseYearMonth(yearMonth);
		StatisticsCalendarResponse response = statisticsService.getCalendar(
			userDetails.getUserId(),
			parsedYearMonth
		);

		return ResponseEntity.ok(
			BaseResponse.onSuccess(StatisticsSuccessCode.CALENDAR_RETRIEVED, response)
		);
	}

	private YearMonth parseYearMonth(String yearMonth) {
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
