package com.Timo.Timo.domain.timer.docs;

import com.Timo.Timo.domain.timer.dto.request.TimerSwitchRequest;
import com.Timo.Timo.domain.timer.dto.response.TimerSwitchResponse;
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

public interface TimerSwitchControllerDocs {

  @Operation(
      summary = "타이머 전환",
      description = """
              진행 중인 타이머를 종료하고, 요청한 투두의 타이머를 바로 시작합니다.
              종료되는 타이머는 중지(stop)와 동일하게 STOPPED 상태로 처리되며 해당 투두는 완료 처리됩니다. AI 피드백은 생성하지 않습니다.
              종료와 시작은 하나의 트랜잭션으로 처리되어 한쪽만 반영되지 않습니다.

              반복 투두는 여러 날짜에 같은 todoId로 노출되므로, body의 date(필수)로 새 타이머가 상태를 반영할 대상 날짜를 지정합니다.
              """
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "타이머 전환 성공",
          useReturnTypeSchema = true
      ),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 데이터 형식(투두 ID 또는 대상 날짜 누락 포함)이거나 전환할 투두에 예상 소요 시간이 설정되지 않은 경우",
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
          description = "본인 소유의 타이머 또는 투두가 아닌 경우",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "존재하지 않는 타이머 또는 투두인 경우",
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
  ResponseEntity<BaseResponse<TimerSwitchResponse>> switchTimer(
      @Parameter(description = "종료할(현재 진행 중인) 타이머 기록 ID", example = "10")
      @PathVariable Long timerId,
      @Valid @RequestBody TimerSwitchRequest request,
      @Parameter(hidden = true) CustomUserDetails userDetails
  );
}
