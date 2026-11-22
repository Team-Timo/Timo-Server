package com.Timo.Timo.domain.calendar.docs;

import com.Timo.Timo.domain.calendar.dto.request.CalendarSyncRequest;
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
