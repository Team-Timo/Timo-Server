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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface TimerControllerDocs {

  @Operation(
      summary = "타이머 시작",
      description = """
			  투두의 타이머를 시작합니다.
		  	한 번에 한 개의 타이머만 실행 가능하며, 이미 실행/일시정지 중인 타이머가 있으면 409를 반환합니다.
      """
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "타이머 시작 성공",
          useReturnTypeSchema = true
      ),
      @ApiResponse(
          responseCode = "400",
          description = "예상 소요 시간이 설정되지 않은 투두인 경우",
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
      @Parameter(hidden = true) CustomUserDetails userDetails
  );
}
