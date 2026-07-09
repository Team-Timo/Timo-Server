package com.Timo.Timo.domain.tag.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.tag.dto.request.TagCreateRequest;
import com.Timo.Timo.domain.tag.dto.response.TagCreateResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface TagControllerDocs {

	@Operation(
			summary = "태그 생성",
			description = """
					신규 태그를 생성합니다. 생성된 태그는 요청한 사용자만 사용할 수 있으며 isDefault는 항상 false입니다.
					모든 사용자가 공유하는 기본 태그 또는 본인이 이미 만든 태그와 이름이 같으면 생성할 수 없습니다.
					"""
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "201",
					description = "태그 생성 성공",
					useReturnTypeSchema = true
			),
			@ApiResponse(
					responseCode = "400",
					description = "태그 이름이 누락되었거나 20자를 초과한 경우",
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
					responseCode = "409",
					description = "이미 존재하는 태그명인 경우",
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
	ResponseEntity<BaseResponse<TagCreateResponse>> createTag(
			@Parameter(hidden = true) CustomUserDetails userDetails,
			TagCreateRequest request
	);
}
