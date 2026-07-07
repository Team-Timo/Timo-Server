package com.Timo.Timo.domain.calendar.docs;

import com.Timo.Timo.domain.calendar.dto.request.CalendarConnectRequest;
import com.Timo.Timo.domain.calendar.dto.response.CalendarConnectResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarDisconnectResponse;
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
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface CalendarControllerDocs {

  @Operation(
      summary = "구글 캘린더 연동",
      description = """
          구글 OAuth 동의 완료 후 발급된 authorizationCode로 구글 토큰을 교환하여 캘린더 연동
          - 가입 시 사용한 구글 계정과 다른 계정으로 연동을 시도하면 거부됨
          """,
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "연동 성공", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "authorizationCode 누락",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "401", description = "구글 인증 실패, 토큰 없음/만료, 또는 가입 이메일과 다른 계정으로 연동 시도",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "409", description = "이미 캘린더가 연동된 상태",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
  })
  ResponseEntity<BaseResponse<CalendarConnectResponse>> connectCalendar(
      @Parameter(hidden = true) CustomUserDetails userDetails,
      @Valid @RequestBody CalendarConnectRequest request
  );

  @Operation(
      summary = "구글 캘린더 연동 해제",
      description = """
          연동된 구글 캘린더 정보를 삭제하고 구글 토큰을 revoke
          """,
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "연동 해제 성공", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "401", description = "토큰 없음/만료/유효하지 않음",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "404", description = "연동된 캘린더가 없는 경우",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
  })
  ResponseEntity<BaseResponse<CalendarDisconnectResponse>> disconnectCalendar(
      @Parameter(hidden = true) CustomUserDetails userDetails
  );
}