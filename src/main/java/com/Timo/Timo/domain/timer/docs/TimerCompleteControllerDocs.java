package com.Timo.Timo.domain.timer.docs;

import com.Timo.Timo.domain.timer.dto.response.TimerFinishResponse;
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
import org.springframework.web.bind.annotation.PathVariable;

public interface TimerCompleteControllerDocs {

  @Operation(
      summary = "타이머 시간 완료",
      description = """
			예상 소요 시간이 모두 경과하여 타이머를 자동 종료합니다.
			종료 시각 기록, 실제 수행 시간 계산 (status → COMPLETED)
			해당 날짜 TodoInstance 완료 처리 및 타이머 상태 초기화
			계획 시간과 실제 수행 기록을 분석한 AI 피드백 반환
			"""
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "타이머 완료 성공",
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
          description = "이미 종료된 타이머인 경우",
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
  ResponseEntity<BaseResponse<TimerFinishResponse>> completeTimer(
      @Parameter(description = "타이머 기록 ID", example = "10")
      @PathVariable Long timerId,
      @Parameter(hidden = true) CustomUserDetails userDetails
  );
}