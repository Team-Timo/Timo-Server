package com.Timo.Timo.domain.user.docs;

import com.Timo.Timo.domain.user.dto.request.OnboardingRequest;
import com.Timo.Timo.domain.user.dto.response.OnboardingResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

public interface OnboardingControllerDocs {

  @Operation(
      summary = "온보딩 완료",
      description = "언어, 예측 정확도, 기상/취침 시간을 저장하고 온보딩을 완료 처리합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "온보딩 완료 성공",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BaseResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청인 경우 (형식 오류, 필드 누락, 범위 초과 등)",
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
  ResponseEntity<BaseResponse<OnboardingResponse>> completeOnboarding(
      CustomUserDetails userDetails,
      OnboardingRequest request
  );
}
