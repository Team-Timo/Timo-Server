package com.Timo.Timo.domain.calendar.controller;

import com.Timo.Timo.domain.calendar.docs.CalendarControllerDocs;
import com.Timo.Timo.domain.calendar.dto.request.CalendarSyncRequest;
import com.Timo.Timo.domain.calendar.dto.response.CalendarSyncResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarSuccessCode;
import com.Timo.Timo.domain.calendar.service.CalendarEventSyncService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "연동된 캘린더 일정 동기화 API")
public class CalendarController implements CalendarControllerDocs {

  private final CalendarEventSyncService calendarEventSyncService;

  @Override
  @PostMapping("/events/sync")
  public ResponseEntity<BaseResponse<CalendarSyncResponse>> syncCalendarEvents(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody(required = false) CalendarSyncRequest request
  ) {
    Long userId = userDetails.getUserId();
    String filter = request != null ? request.filter() : null;
    String baseDate = request != null ? request.baseDate() : null;

    CalendarSyncResponse response = calendarEventSyncService.syncEvents(userId, filter, baseDate);

    return ResponseEntity.ok(
        BaseResponse.onSuccess(CalendarSuccessCode.CALENDAR_EVENTS_SYNCED, response)
    );
  }
}
