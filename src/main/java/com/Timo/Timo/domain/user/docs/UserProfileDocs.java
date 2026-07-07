package com.Timo.Timo.domain.user.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.user.dto.response.UserProfileResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

public interface UserProfileDocs {

	@Operation(
		summary = "내 프로필 조회",
		description = """
			현재 로그인한 사용자의 프로필 정보를 조회합니다.

			Swagger UI 오른쪽 위의 Authorize 버튼을 눌러 유효한 Access Token을 입력해야 합니다.
			Access Token의 사용자 ID와 일치하는 사용자의 정보가 반환됩니다.
			""",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "프로필 조회 성공",
			useReturnTypeSchema = true
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
	ResponseEntity<BaseResponse<UserProfileResponse>> getMyProfile(
		@Parameter(hidden = true) CustomUserDetails userDetails
	);
}
