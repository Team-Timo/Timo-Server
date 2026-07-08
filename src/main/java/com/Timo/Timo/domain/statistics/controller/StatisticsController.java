package com.Timo.Timo.domain.statistics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Timo.Timo.domain.statistics.docs.StatisticsCalendarDocs;
import com.Timo.Timo.domain.statistics.docs.StatisticsDailyDocs;
import com.Timo.Timo.domain.statistics.docs.StatisticsSummaryDocs;
import com.Timo.Timo.domain.statistics.dto.response.StatisticsCalendarResponse;
import com.Timo.Timo.domain.statistics.dto.response.StatisticsDailyResponse;
import com.Timo.Timo.domain.statistics.dto.response.StatisticsSummaryResponse;
import com.Timo.Timo.domain.statistics.exception.StatisticsSuccessCode;
import com.Timo.Timo.domain.statistics.service.StatisticsService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "통계 API")
public class StatisticsController implements StatisticsCalendarDocs, StatisticsSummaryDocs, StatisticsDailyDocs {

	private final StatisticsService statisticsService;

	@Override
	@GetMapping("/calendar")
	public ResponseEntity<BaseResponse<StatisticsCalendarResponse>> getCalendar(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(required = false) String yearMonth
	) {
		StatisticsCalendarResponse response = statisticsService.getCalendar(
			userDetails.getUserId(),
			yearMonth
		);

		return ResponseEntity.ok(
			BaseResponse.onSuccess(StatisticsSuccessCode.CALENDAR_RETRIEVED, response)
		);
	}

	@Override
	@GetMapping("/summary")
	public ResponseEntity<BaseResponse<StatisticsSummaryResponse>> getSummary(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(required = false) String yearMonth
	) {
		StatisticsSummaryResponse response = statisticsService.getSummary(
			userDetails.getUserId(),
			yearMonth
		);

		return ResponseEntity.ok(
			BaseResponse.onSuccess(StatisticsSuccessCode.SUMMARY_RETRIEVED, response)
		);
	}

	@Override
	@GetMapping("/daily")
	public ResponseEntity<BaseResponse<StatisticsDailyResponse>> getDaily(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(required = false) String date
	) {
		StatisticsDailyResponse response = statisticsService.getDaily(
			userDetails.getUserId(),
			date
		);

		return ResponseEntity.ok(
			BaseResponse.onSuccess(StatisticsSuccessCode.DAILY_RETRIEVED, response)
		);
	}
}
