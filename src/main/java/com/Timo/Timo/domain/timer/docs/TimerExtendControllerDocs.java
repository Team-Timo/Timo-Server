package com.Timo.Timo.domain.timer.docs;

import com.Timo.Timo.domain.timer.dto.request.TimerExtendRequest;
import com.Timo.Timo.domain.timer.dto.response.TimerExtendResponse;
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

public interface TimerExtendControllerDocs {

  @Operation(
      summary = "타이머 연장",
      description = """
              사용자가 입력한 연장 시간을 현재 타이머에 반영합니다.
              연장 시간(분)을 초로 변환하여 extended_seconds에 누적
              RUNNING, PAUSED 상태 모두에서 호출 가능하며, 타이머 상태(status)는 변경되지 않습니다.
              """
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "타이머 연장 성공",
          useReturnTypeSchema = true
      ),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 데이터 형식 (연장 시간 값 오류)",
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
  ResponseEntity<BaseResponse<TimerExtendResponse>> extendTimer(
      @Parameter(description = "타이머 기록 ID", example = "10")
      @PathVariable Long timerId,
      @Valid @RequestBody TimerExtendRequest request,
      @Parameter(hidden = true) CustomUserDetails userDetails
  );
}
