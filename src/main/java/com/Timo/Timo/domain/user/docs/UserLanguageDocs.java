package com.Timo.Timo.domain.user.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.user.dto.request.UpdateLanguageRequest;
import com.Timo.Timo.domain.user.dto.response.UpdateLanguageResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface UserLanguageDocs {

	@Operation(
		summary = "서비스 언어 수정",
		description = """
			현재 로그인한 사용자의 서비스 언어를 변경합니다.

			변경 가능한 값은 `KO`, `EN`입니다.
			이름, 이메일, 프로필 이미지는 구글 계정 기반 정보이므로 해당 API에서 변경할 수 없습니다.
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "언어 설정 수정 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "language가 누락되었거나 KO, EN 이외의 값인 경우",
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
			description = "Access Token의 사용자 ID에 해당하는 사용자를 찾을 수 없는 경우",
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
	ResponseEntity<BaseResponse<UpdateLanguageResponse>> updateLanguage(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "변경할 서비스 언어",
			content = @Content(
				schema = @Schema(implementation = UpdateLanguageRequest.class)
			)
		)
		UpdateLanguageRequest request
	);
}
