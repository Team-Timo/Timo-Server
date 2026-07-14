package com.Timo.Timo.domain.terms.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.terms.dto.response.TermsDetailResponse;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface TermsControllerDocs {
	@Operation(
		summary = "약관 조건 조회",
		description = """
			약관 타입과 언어 기준으로 최신 약관 1건을 조회합니다.
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "약관 조회 성공, 조건에 맞는 약관이 없으면 data는 null",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 타입 또는 언어 값",
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
	ResponseEntity<BaseResponse<TermsDetailResponse>> getTermsByCondition(
		@Parameter(description = "약관 타입", example = "SERVICE") String type,
		@Parameter(description = "약관 언어", example = "KO") String language
	);
}
