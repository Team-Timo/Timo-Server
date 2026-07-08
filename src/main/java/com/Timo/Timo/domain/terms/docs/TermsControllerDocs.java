package com.Timo.Timo.domain.terms.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.terms.dto.response.TermsListResponse;
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
		summary = "약관 내용 조회",
		description = """
			서비스 이용약관 및 개인정보 처리방침을 조회합니다.
			type을 지정하지 않으면 전체 약관을 조회하고, SERVICE 또는 PRIVACY를 지정하면 해당 약관만 조회합니다.
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "약관 조회 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 type 값",
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
	ResponseEntity<BaseResponse<TermsListResponse>> getTerms(
		@Parameter(description = "약관 타입(SERVICE, PRIVACY)", example = "SERVICE") String type
	);
}
