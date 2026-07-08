package com.Timo.Timo.domain.timer.docs;

import com.Timo.Timo.domain.timer.dto.request.TimerActionRequest;
import com.Timo.Timo.domain.timer.dto.response.TimerStatusResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface TimerStatusControllerDocs {

  @Operation(
      summary = "타이머 일시정지/재개",
      description = """
			실행 중인 타이머의 일시정지 / 재개를 처리합니다.<br>
			PAUSE: 현재 세션의 paused_at 기록, status → PAUSED
			RESUME: 새 세션 생성, status → RUNNING
			"""
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "상태 변경 성공",
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
          responseCode = "403",
          description = "본인 소유의 타이머가 아닌 경우",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "존재하지 않는 타이머인 경우",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "409",
          description = "잘못된 상태 전이 (PAUSED 상태에서 PAUSE 요청, 종료된 타이머 조작 등)",
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
  ResponseEntity<BaseResponse<TimerStatusResponse>> changeStatus(
      @Parameter(description = "타이머 기록 ID", example = "10")
      @PathVariable Long timerId,
      @Valid @RequestBody TimerActionRequest request,
      @Parameter(hidden = true) CustomUserDetails userDetails
  );
}