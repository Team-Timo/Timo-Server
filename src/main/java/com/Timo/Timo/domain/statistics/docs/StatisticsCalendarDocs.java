package com.Timo.Timo.domain.statistics.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.statistics.dto.response.StatisticsCalendarResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface StatisticsCalendarDocs {

	@Operation(
		summary = "통계 캘린더 조회",
		description = """
			지정한 연월의 일별 투두 완료 현황을 통계 캘린더에 표시하기 위한 API입니다.

			- yearMonth는 yyyy-MM 형식으로 전달합니다.
			- today는 UTC 기준 오늘 날짜입니다.
			- 조회 월의 모든 날짜를 날짜 오름차순으로 반환합니다.
			- 날짜가 오늘 이후여도 작성된 투두가 있다면 완료율을 계산해서 반환합니다.
			- 해당 날짜에 투두가 없으면 completionRate는 `0`으로 반환합니다.
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "통계 캘린더 조회 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "yearMonth 누락, 형식 오류, 유효하지 않은 연월",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorDto.class)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "Access Token이 없거나 만료되었거나 유효하지 않은 경우",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorDto.class)
			)
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorDto.class)
			)
		)
	})
	ResponseEntity<BaseResponse<StatisticsCalendarResponse>> getCalendar(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@Parameter(
			description = "조회할 연월. yyyy-MM 형식",
			required = true,
			example = "2026-06"
		)
		String yearMonth
	);
}