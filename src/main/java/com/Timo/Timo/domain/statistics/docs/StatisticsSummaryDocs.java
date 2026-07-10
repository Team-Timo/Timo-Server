package com.Timo.Timo.domain.statistics.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.statistics.dto.response.StatisticsSummaryResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface StatisticsSummaryDocs {

	@Operation(
		summary = "월별 통계 요약 조회",
		description = """
			지정한 연월의 전체 기록 시간, 활동일, 일평균, 누적 태스크를 조회합니다.

			- 전체 기록 시간: 해당 월에 기록된 타이머 시간의 총합
			- 활동일: 해당 월에 1개 이상의 투두를 생성한 날짜 수
			- 일평균: 타이머를 1회 이상 실행한 날짜들의 기록 시간 총합을 해당 날짜 수로 나눈 값
			- 누적 태스크: 해당 월에 작성된 전체 투두 수와 그중 완료한 투두 수
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "월별 통계 요약 조회 성공",
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
	ResponseEntity<BaseResponse<StatisticsSummaryResponse>> getSummary(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@Parameter(
			description = "조회할 연월. yyyy-MM 형식",
			required = true,
			example = "2026-07"
		)
		String yearMonth
	);
}