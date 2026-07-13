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
		summary = "약관 상세 조회",
		description = """
			약관 ID로 특정 약관의 상세 내용을 조회합니다.
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "약관 조회 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "404",
			description = "존재하지 않는 약관 ID",
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
	ResponseEntity<BaseResponse<TermsDetailResponse>> getTerms(
		@Parameter(description = "약관 ID", example = "1") Long termsId
	);
}