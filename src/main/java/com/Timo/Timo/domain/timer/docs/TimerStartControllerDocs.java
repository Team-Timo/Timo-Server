package com.Timo.Timo.domain.timer.docs;

import com.Timo.Timo.domain.timer.dto.response.TimerStartResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface TimerStartControllerDocs {

  @Operation(
      summary = "타이머 시작",
      description = """
              투두의 타이머를 시작합니다.
              한 번에 한 개의 타이머만 실행 가능하며, 이미 실행/일시정지 중인 타이머가 있으면 409를 반환합니다.

              반복 투두는 여러 날짜에 같은 todoId로 노출되므로, 실행중 표시는 todoId가 아닌 (todoId, date) 조합으로 구분해야 합니다.
              쿼리 파라미터 date로 타이머가 상태를 반영할 대상 날짜를 지정할 수 있으며(미래/과거 포함), 미지정 시 사용자 타임존 기준 오늘로 처리됩니다. 이후 일시정지/재개/완료/중지는 이 날짜를 그대로 사용합니다.
              """
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "타이머 시작 성공",
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
          description = "본인 소유의 투두가 아닌 경우",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "존재하지 않는 투두인 경우",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "409",
          description = "이미 실행 중인 타이머가 있는 경우",
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
  ResponseEntity<BaseResponse<TimerStartResponse>> startTimer(
      @Parameter(description = "타이머를 시작할 투두 ID", example = "3")
      @PathVariable Long todoId,
      @Parameter(description = "타이머가 상태를 반영할 대상 날짜(미지정 시 오늘). ISO 형식", example = "2026-07-20")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @Parameter(hidden = true) CustomUserDetails userDetails
  );
}
