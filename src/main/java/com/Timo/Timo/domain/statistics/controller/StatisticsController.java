package com.Timo.Timo.domain.statistics.controller;

import java.time.YearMonth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Timo.Timo.domain.statistics.docs.StatisticsCalendarDocs;
import com.Timo.Timo.domain.statistics.dto.response.StatisticsCalendarResponse;
import com.Timo.Timo.domain.statistics.exception.StatisticsSuccessCode;
import com.Timo.Timo.domain.statistics.service.StatisticsService;
import com.Timo.Timo.domain.statistics.support.StatisticsDateParser;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "통계 API")
public class StatisticsController implements StatisticsCalendarDocs {

	private final StatisticsService statisticsService;
	private final StatisticsDateParser statisticsDateParser;

	@Override
	@GetMapping("/calendar")
	public ResponseEntity<BaseResponse<StatisticsCalendarResponse>> getCalendar(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(required = false) String yearMonth
	) {
		YearMonth parsedYearMonth = statisticsDateParser.parseYearMonth(yearMonth);
		StatisticsCalendarResponse response = statisticsService.getCalendar(
			userDetails.getUserId(),
			parsedYearMonth
		);

		return ResponseEntity.ok(
			BaseResponse.onSuccess(StatisticsSuccessCode.CALENDAR_RETRIEVED, response)
		);
	}
}