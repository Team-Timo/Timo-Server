package com.Timo.Timo.domain.calendar.docs;

import com.Timo.Timo.domain.calendar.dto.request.CalendarSyncRequest;
import com.Timo.Timo.domain.calendar.dto.response.CalendarEventsResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarSyncResponse;
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
import org.springframework.http.ResponseEntity;

public interface CalendarControllerDocs {

  @Operation(
      summary = "캘린더 일정 조회",
      description = """
        filter(DAY/WEEK/TWO_WEEK)와 baseDate에 따라 연동된 모든 캘린더(구글·애플)의 일정을 일자별로 합쳐 조회합니다.
        일정마다 calendarSource(GOOGLE/APPLE)로 어느 캘린더의 일정인지 표시합니다.
        
        DAY: baseDate 하루
        
        WEEK: baseDate ~ baseDate+6일 (총 7일)
        
        TWO_WEEK: baseDate-7일 ~ baseDate+7일 (총 15일)
        
        baseDate 미입력 시 오늘 날짜가 기본값으로 사용됩니다.
        별도 저장 없이 매 요청마다 각 캘린더 API를 직접 호출하여 최신 상태를 반환합니다.
        한 캘린더만 연동되어 있으면 그 캘린더의 일정만 반환하고, 연동된 캘린더가 하나도 없으면 404(CALENDAR_404)를 반환합니다.

        한 캘린더 조회가 실패해도(인증 만료, 캘린더 서버 장애 등) 나머지 캘린더는 계속 조회하며 응답은 200입니다.
        실패한 캘린더는 failedSources에 calendarSource와 errorCode(예: GOOGLE_CALENDAR_401, APPLE_CALENDAR_401)로 담기며, 해당 캘린더의 일정은 응답에 포함되지 않습니다.
        """,
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공 (일부 캘린더가 실패하면 failedSources에 포함)", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 filter 값이거나 날짜 형식 오류",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "401", description = "Access Token 없음/만료/유효하지 않음(COMMON_401)",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "404", description = "연동된 캘린더가 하나도 없는 경우(CALENDAR_404)",
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
      summary = "캘린더 일정 동기화",
      description = """
        filter(DEFAULT/WEEK)와 baseDate 범위의 연동된 모든 캘린더(구글·애플) 일정을 투두로 저장/갱신합니다.
        캘린더마다 따로 동기화하며, 각 투두는 calendarSource로 출처를 구분합니다.
        한 캘린더 동기화가 실패해도(인증 만료, 캘린더 서버 장애 등) 나머지 캘린더는 계속 동기화하며 응답은 200입니다.
        실패한 캘린더는 failedSources에 calendarSource와 errorCode(예: GOOGLE_CALENDAR_401, APPLE_CALENDAR_401)로 담기며, 해당 캘린더의 투두는 변경되지 않습니다.
        errorCode가 GOOGLE_CALENDAR_401이면 구글 캘린더 재연동을, APPLE_CALENDAR_401이면 앱 전용 암호가 삭제·변경된 것이므로 애플 캘린더 재연동을 안내해야 합니다.

        DEFAULT: baseDate-7일 ~ baseDate+7일 (총 15일)

        WEEK: baseDate ~ baseDate+6일 (총 7일)

        filter, baseDate 미입력 시 각각 DEFAULT, 오늘 날짜가 기본값으로 사용됩니다.
        홈 조회(/api/v1/home, /api/v1/home/today)를 호출하기 전에 이 API를 먼저 호출해야 캘린더 일정이 함께 조회됩니다.

        캘린더에 새로 생긴 일정은 투두로 저장되고, 제목이 바뀐 일정은 제목이 갱신됩니다.
        날짜가 바뀐 일정은 기존 투두가 새 날짜로 이동하며, 티모에서 설정한 소요 시간·태그·아이콘·메모·하위 태스크·완료 상태와 타이머 기록은 그대로 유지됩니다.
        해당 투두의 타이머가 실행 중이거나 일시정지 상태이면 이동하지 않고, 타이머를 종료한 뒤 다음 동기화 때 이동합니다.
        
        캘린더에서 삭제된 일정은 타이머 기록이 없으면 삭제되고, 기록이 있으면 삭제되지 않고 isDeletedFromCalendar=true로 표시됩니다.
        소요 시간은 0으로 저장되며, 제목이 30자를 넘으면 30자까지만 저장됩니다. (1차 스프린트 임시 해결책)
        """,
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "동기화 성공 (일부 캘린더가 실패하면 failedSources에 포함)", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 filter 값이거나 날짜 형식 오류",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "401", description = "Access Token 없음/만료/유효하지 않음(COMMON_401)",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "404", description = "연동된 캘린더가 하나도 없는 경우(CALENDAR_404)",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
  })
  ResponseEntity<BaseResponse<CalendarSyncResponse>> syncCalendarEvents(
      @Parameter(hidden = true) CustomUserDetails userDetails,
      CalendarSyncRequest request
  );
}
