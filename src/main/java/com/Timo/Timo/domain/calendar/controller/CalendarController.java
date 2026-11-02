package com.Timo.Timo.domain.calendar.controller;

import com.Timo.Timo.domain.calendar.docs.CalendarControllerDocs;
import com.Timo.Timo.domain.calendar.dto.request.CalendarSyncRequest;
import com.Timo.Timo.domain.calendar.dto.response.CalendarEventsResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarSuccessCode;
import com.Timo.Timo.domain.calendar.service.CalendarEventQueryService;
import com.Timo.Timo.domain.calendar.service.CalendarEventSyncService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "연동된 캘린더 일정 조회·동기화 API")
public class CalendarController implements CalendarControllerDocs {

  private final CalendarEventQueryService calendarEventQueryService;
  private final CalendarEventSyncService calendarEventSyncService;

  @Override
  @GetMapping("/events")
  public ResponseEntity<BaseResponse<CalendarEventsResponse>> getCalendarEvents(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "조회 필터", example = "WEEK") @RequestParam String filter,
      @Parameter(description = "기준 날짜 (YYYY-MM-DD), 미입력 시 오늘", example = "2026-07-14")
      @RequestParam(required = false) String baseDate
  ) {
    Long userId = userDetails.getUserId();
    CalendarEventsResponse response = calendarEventQueryService.getEvents(userId, filter, baseDate);

    return ResponseEntity.ok(
        BaseResponse.onSuccess(CalendarSuccessCode.CALENDAR_EVENTS_RETRIEVED, response)
    );
  }

  @Override
  @PostMapping("/events/sync")
  public ResponseEntity<BaseResponse<Object>> syncCalendarEvents(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody(required = false) CalendarSyncRequest request
  ) {
    Long userId = userDetails.getUserId();
    String filter = request != null ? request.filter() : null;
    String baseDate = request != null ? request.baseDate() : null;

    calendarEventSyncService.syncEvents(userId, filter, baseDate);

    return ResponseEntity.ok(
        BaseResponse.onSuccess(CalendarSuccessCode.CALENDAR_EVENTS_SYNCED, Map.of())
    );
  }
}
