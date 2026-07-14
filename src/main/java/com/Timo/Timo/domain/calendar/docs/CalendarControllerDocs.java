package com.Timo.Timo.domain.calendar.docs;

import com.Timo.Timo.domain.calendar.dto.request.CalendarConnectRequest;
import com.Timo.Timo.domain.calendar.dto.response.CalendarAuthorizeResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarConnectResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarDisconnectResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarEventsResponse;
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
      summary = "구글 캘린더 연동 시작",
      description = """
			구글 캘린더 연동을 시작하는 구글 인증 URL을 발급합니다.
			
			프론트는 이 응답의 authorizationUrl로 window.location.assign 등을 통해 직접 이동해야 합니다.
			""",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "인증 URL 발급 성공",
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
  ResponseEntity<BaseResponse<CalendarAuthorizeResponse>> authorize(
      @Parameter(hidden = true) CustomUserDetails userDetails
  );

  @Operation(
      summary = "구글 캘린더 연동",
      description = """
		  구글 OAuth 동의 완료 후 발급된 authorizationCode와 state로 구글 토큰을 교환하여 캘린더를 연동합니다.
		  
		  state는 authorize API 호출 시 발급받은 값을 그대로 전달해야 하며, 검증 후 즉시 만료됩니다.
		  
		  가입 시 사용한 구글 계정과 다른 계정으로 연동을 시도하면 거부됩니다.
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
      summary = "캘린더 일정 조회",
      description = """
        filter(DAY/WEEK/TWO_WEEK)와 baseDate에 따라 연동된 구글 캘린더 일정을 일자별로 조회합니다.
        - DAY: baseDate 하루
        - WEEK: baseDate ~ baseDate+6일 (총 7일)
        - TWO_WEEK: baseDate-6일 ~ baseDate+6일 (총 13일)
        baseDate 미입력 시 오늘 날짜가 기본값으로 사용됩니다.
        별도 저장 없이 매 요청마다 구글 API를 직접 호출하여 최신 상태를 반환합니다.
        Swagger UI 오른쪽 위의 Authorize 버튼을 눌러 유효한 Access Token을 입력해야 합니다.
        """,
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 filter 값이거나 날짜 형식 오류",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "401", description = "토큰 없음/만료/유효하지 않음",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "404", description = "연동된 캘린더가 없는 경우",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
  })
  ResponseEntity<BaseResponse<CalendarEventsResponse>> getCalendarEvents(
      @Parameter(hidden = true) CustomUserDetails userDetails,
      @Parameter(description = "조회 필터", example = "WEEK") String filter,
      @Parameter(description = "기준 날짜 (YYYY-MM-DD), 미입력 시 오늘", example = "2026-07-14") String baseDate
  );

  @Operation(
      summary = "구글 캘린더 연동 해제",
      description = """
          연동된 구글 캘린더 정보를 삭제하고 구글 토큰을 revoke합니다.
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
