package com.Timo.Timo.domain.calendar.docs;

import com.Timo.Timo.domain.calendar.dto.request.CalendarConnectRequest;
import com.Timo.Timo.domain.calendar.dto.request.CalendarSyncRequest;
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

			redirectOrigin을 전달하면 {redirectOrigin}/oauth/calendar/callback을 redirect_uri로 사용합니다.
			미입력이면 기본 프론트 주소로 redirect되고, 허용되지 않은 origin이면 400(COMMON_400)을 반환합니다.
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
          responseCode = "400",
          description = "허용되지 않은 redirectOrigin인 경우",
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
  ResponseEntity<BaseResponse<CalendarAuthorizeResponse>> authorize(
      @Parameter(hidden = true) CustomUserDetails userDetails,
      @Parameter(description = "연동 완료 후 돌아올 프론트 origin (미입력 시 기본 프론트 주소, 미허용 시 400)", example = "http://localhost:3000")
      String redirectOrigin
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
        
        DAY: baseDate 하루
        
        WEEK: baseDate ~ baseDate+6일 (총 7일)
        
        TWO_WEEK: baseDate-7일 ~ baseDate+7일 (총 15일)
        
        baseDate 미입력 시 오늘 날짜가 기본값으로 사용됩니다.
        별도 저장 없이 매 요청마다 구글 API를 직접 호출하여 최신 상태를 반환합니다.
        """,
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 filter 값이거나 날짜 형식 오류",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "401", description = "Access Token 없음/만료/유효하지 않음, 또는 구글 access token 갱신 실패 등 구글 인증 자체 실패",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "404", description = "연동된 캘린더가 없는 경우",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "429", description = "구글 캘린더 API 요청이 일시적으로 제한된 경우",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "502", description = "구글 캘린더 서버와의 통신 중 오류가 발생한 경우",
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
      summary = "구글 캘린더 일정 동기화",
      description = """
        filter(DEFAULT/WEEK)와 baseDate 범위의 구글 캘린더 일정을 투두로 저장/갱신합니다.

        DEFAULT: baseDate-7일 ~ baseDate+7일 (총 15일)

        WEEK: baseDate ~ baseDate+6일 (총 7일)

        filter, baseDate 미입력 시 각각 DEFAULT, 오늘 날짜가 기본값으로 사용됩니다.
        홈 조회(/api/v1/home, /api/v1/home/today)를 호출하기 전에 이 API를 먼저 호출해야 구글 일정이 함께 조회됩니다.

        구글에서 새로 생긴 일정은 투두로 저장되고, 제목/날짜가 바뀐 일정은 갱신됩니다.
        구글에서 삭제된 일정은 타이머 기록이 없으면 삭제되고, 기록이 있으면 삭제되지 않고 구글에서 삭제된 일정으로 표시됩니다.
        소요 시간은 0으로 저장되며, 제목이 30자를 넘으면 30자까지만 저장됩니다. (1차 스프린트 임시 해결책)
        """,
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "동기화 성공", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 filter 값이거나 날짜 형식 오류",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "401", description = "Access Token 없음/만료/유효하지 않음, 또는 구글 access token 갱신 실패 등 구글 인증 자체 실패",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "404", description = "연동된 캘린더가 없는 경우",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "429", description = "구글 캘린더 API 요청이 일시적으로 제한된 경우",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "502", description = "구글 캘린더 서버와의 통신 중 오류가 발생한 경우",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
  })
  ResponseEntity<BaseResponse<Object>> syncCalendarEvents(
      @Parameter(hidden = true) CustomUserDetails userDetails,
      CalendarSyncRequest request
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
