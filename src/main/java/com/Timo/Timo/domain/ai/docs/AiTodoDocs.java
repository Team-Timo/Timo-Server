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
		tags = "AI",
		summary = "AI 예상 소요 시간 추천",
		description = """
			투두명과 태그를 기준으로 예상 소요 시간을 추천합니다.

			서버는 사용자의 타이머 기반 실제 소요시간 기록을 조회해 Gemini에 전달합니다.
			1. 현재 투두명과 비슷한 과거 투두의 실제 소요시간 기록
			2. 사용자가 지정한 태그의 최근 실제 소요시간 기록
			3. 기록이 없으면 현재 투두명 기준

			Gemini는 위 기록을 종합해 예상 소요 시간을 생성합니다.
			RPM, RPD, TPM 제한을 초과하면 Gemini 호출 전 429 응답을 반환합니다.
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
			responseCode = "429",
			description = "AI 추천 요청 횟수 제한을 초과한 경우",
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