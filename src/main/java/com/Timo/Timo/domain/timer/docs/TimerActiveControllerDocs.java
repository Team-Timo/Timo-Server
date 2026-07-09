package com.Timo.Timo.domain.timer.docs;

import com.Timo.Timo.domain.timer.dto.response.TimerActiveResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

public interface TimerActiveControllerDocs {

  @Operation(
      summary = "현재 실행 중인 타이머 조회",
      description = """
			로그인한 사용자의 현재 실행 중(RUNNING/PAUSED)인 타이머를 단건 조회합니다.
			한 사용자당 시작 이후 완료/종료되지 않은 타이머는 최대 1개만 존재할 수 있습니다.
			실행 중인 타이머가 없으면 data: null을 반환합니다.
			"""
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "조회 성공 (실행 중인 타이머 유무와 무관하게 200 반환)",
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
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      )
  })
  ResponseEntity<BaseResponse<TimerActiveResponse>> getActiveTimer(
      @Parameter(hidden = true) CustomUserDetails userDetails
  );
}
