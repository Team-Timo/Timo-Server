package com.Timo.Timo.domain.calendar.apple.docs;

import com.Timo.Timo.domain.calendar.apple.dto.request.AppleCalendarConnectRequest;
import com.Timo.Timo.domain.calendar.apple.dto.response.AppleCalendarConnectResponse;
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

public interface AppleCalendarControllerDocs {

  @Operation(
      summary = "애플 캘린더 연동",
      description = """
          Apple ID 이메일과 앱 전용 암호로 iCloud 캘린더(CalDAV)를 연동합니다.

          앱 전용 암호는 사용자가 account.apple.com > 로그인 및 보안 > 앱 전용 암호에서 발급합니다. (Apple ID 이중 인증 필요)
          Apple ID는 Timo 가입 이메일과 달라도 됩니다.

          연동 시 iCloud에 실제로 접속해 인증 정보를 확인하며, 틀리면 401(APPLE_CALENDAR_401)을 반환합니다.
          앱 전용 암호는 암호화해 저장하며 응답에 포함하지 않습니다.

          연동 후 일정 조회·동기화는 공통 캘린더 API(/api/v1/users/calendar/events, /events/sync)를 사용합니다.
          일정을 지원하는 모든 iCloud 캘린더(공유받은 캘린더 포함)의 일정을 가져오며, 미리 알림과 구독 캘린더는 제외합니다.
          """,
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "연동 성공", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "appleId·appPassword 누락 또는 appleId가 이메일 형식이 아님",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "401", description = "토큰 없음/만료(COMMON_401), Apple ID 또는 앱 전용 암호가 틀림(APPLE_CALENDAR_401)",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "409", description = "이미 애플 캘린더가 연동된 상태(APPLE_CALENDAR_409)",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "429", description = "애플 요청 제한(APPLE_CALENDAR_429)",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "502", description = "애플 서버 통신 오류(APPLE_CALENDAR_502)",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "503", description = "애플 서버 응답 지연(APPLE_CALENDAR_503)",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
  })
  ResponseEntity<BaseResponse<AppleCalendarConnectResponse>> connectCalendar(
      @Parameter(hidden = true) CustomUserDetails userDetails,
      @Valid @RequestBody AppleCalendarConnectRequest request
  );

  @Operation(
      summary = "애플 캘린더 연동 해제",
      description = """
          저장된 애플 캘린더 연결 정보(Apple ID, 앱 전용 암호)를 삭제합니다.
          애플은 앱 전용 암호 폐기 API가 없으므로, 사용자에게 account.apple.com에서 앱 전용 암호를 직접 삭제하도록 안내해야 합니다.

          해제 시점 기준 오늘 이후의 애플 일정 투두 중 타이머 기록이 없는 투두는 삭제됩니다.
          타이머 기록이 있거나 타이머가 실행 중인 투두는 삭제되지 않고 남으며, 오늘과 지난 애플 일정 투두도 유지됩니다.
          남은 애플 일정 투두는 isDisconnectedFromCalendar=true로 표시되어 삭제할 수 있게 됩니다.
          다시 연동하여 동기화하면 애플에 남아 있는 일정은 다시 isDisconnectedFromCalendar=false로 표시됩니다.
          구글 캘린더에서 가져온 투두에는 영향을 주지 않습니다.
          """,
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "연동 해제 성공", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "401", description = "토큰 없음/만료/유효하지 않음",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "404", description = "연동된 애플 캘린더가 없는 경우(APPLE_CALENDAR_404)",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
  })
  ResponseEntity<BaseResponse<CalendarDisconnectResponse>> disconnectCalendar(
      @Parameter(hidden = true) CustomUserDetails userDetails
  );
}
