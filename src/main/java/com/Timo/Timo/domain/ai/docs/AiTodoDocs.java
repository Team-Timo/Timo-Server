package com.Timo.Timo.domain.ai.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.ai.dto.request.RecommendDurationRequest;
import com.Timo.Timo.domain.ai.dto.response.RecommendDurationResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface AiTodoDocs {

	@Operation(
		summary = "AI 예상 소요 시간 추천",
		description = """
			투두명과 태그를 기준으로 예상 소요 시간을 추천합니다.

			서버는 사용자 과거 투두 기록을 아래 우선순위로 조회해 Gemini에 전달합니다.
			1. 비슷한 투두명 기록
			2. 같은 태그 기록
			3. 최근 기록
			4. 데이터가 부족하면 현재 투두 기준

			Gemini API 키가 없거나 호출/응답 검증에 실패하면 서버 fallback 로직으로 추천 시간을 반환합니다.
			추천 시간은 5분 단위이며, 서비스 정책 범위 안으로 보정됩니다.
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "AI 예상 소요 시간 추천 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "투두명이 누락되었거나 형식이 올바르지 않은 경우",
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
	ResponseEntity<BaseResponse<RecommendDurationResponse>> recommendDuration(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "예상 소요 시간을 추천받을 투두 정보",
			content = @Content(schema = @Schema(implementation = RecommendDurationRequest.class))
		)
		RecommendDurationRequest request
	);
}
