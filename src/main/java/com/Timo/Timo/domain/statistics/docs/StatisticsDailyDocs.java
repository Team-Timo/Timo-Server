package com.Timo.Timo.domain.statistics.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.statistics.dto.response.StatisticsDailyResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface StatisticsDailyDocs {

	@Operation(
		summary = "일별 기록 조회",
		description = """
			특정 날짜의 총 기록시간과 해당 날짜에 계획된 투두 목록을 조회합니다.

			투두별로 투두명, 실제 소요 시간, 예상 소요 시간, 태그 정보를 제공합니다.
			실제 소요 시간은 해당 날짜의 타이머 기록 합계이며, 기록이 없으면 0분입니다.
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "일별 기록 조회 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "date 누락, 형식 오류, 유효하지 않은 날짜",
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
			responseCode = "404",
			description = "사용자 정보를 찾을 수 없는 경우",
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
	ResponseEntity<BaseResponse<StatisticsDailyResponse>> getDaily(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@Parameter(
			description = "조회 날짜. yyyy-MM-dd 형식",
			required = true,
			example = "2026-06-28"
		)
		String date
	);
}
