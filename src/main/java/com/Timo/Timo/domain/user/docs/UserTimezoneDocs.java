package com.Timo.Timo.domain.user.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.user.dto.request.UpdateTimezoneRequest;
import com.Timo.Timo.domain.user.dto.response.UpdateTimezoneResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface UserTimezoneDocs {

	@Operation(
		summary = "시간대 수정",
		description = """
			현재 로그인한 사용자의 시간대를 변경합니다.

			`zoneId`는 IANA 시간대 ID(예: `Asia/Seoul`, `America/New_York`, `UTC`)여야 합니다.
			클라이언트는 기기의 실제 시간대를 감지해 전달하는 것을 권장합니다.
			홈 화면의 '오늘' 판정 등 서버의 날짜 계산이 이 값을 기준으로 동작합니다.
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "시간대 설정 수정 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "zoneId가 누락되었거나 유효한 IANA 시간대 ID가 아닌 경우",
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
	ResponseEntity<BaseResponse<UpdateTimezoneResponse>> updateTimezone(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "변경할 시간대 ID",
			content = @Content(
				schema = @Schema(implementation = UpdateTimezoneRequest.class)
			)
		)
		UpdateTimezoneRequest request
	);
}
