package com.Timo.Timo.domain.calendar.controller;

import com.Timo.Timo.domain.calendar.docs.CalendarControllerDocs;
import com.Timo.Timo.domain.calendar.dto.request.CalendarConnectRequest;
import com.Timo.Timo.domain.calendar.dto.response.CalendarAuthorizeResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarConnectResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarDisconnectResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarEventsResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarSuccessCode;
import com.Timo.Timo.domain.calendar.service.CalendarEventQueryService;
import com.Timo.Timo.domain.calendar.service.CalendarService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "구글 캘린더 연동 API")
public class CalendarController implements CalendarControllerDocs {

  private final CalendarService calendarService;
  private final CalendarEventQueryService calendarEventQueryService;

  @Override
  @GetMapping("/authorize")
  public ResponseEntity<BaseResponse<CalendarAuthorizeResponse>> authorize(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    String url = calendarService.buildAuthorizationUrl(userDetails.getUserId());
    return ResponseEntity.ok(
        BaseResponse.onSuccess(CalendarSuccessCode.CALENDAR_AUTHORIZE_URL_ISSUED,
            new CalendarAuthorizeResponse(url))
    );
  }

  @Override
  @PostMapping
  public ResponseEntity<BaseResponse<CalendarConnectResponse>> connectCalendar(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody CalendarConnectRequest request
  ) {
    Long userId = userDetails.getUserId();
    CalendarConnectResponse response = calendarService.connect(userId, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(
        BaseResponse.onSuccess(CalendarSuccessCode.CALENDAR_CONNECTED, response)
    );
  }

  @Override
  @DeleteMapping
  public ResponseEntity<BaseResponse<CalendarDisconnectResponse>> disconnectCalendar(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId();
    CalendarDisconnectResponse response = calendarService.disconnect(userId);

    return ResponseEntity.ok(
        BaseResponse.onSuccess(CalendarSuccessCode.CALENDAR_DISCONNECTED, response)
    );
  }

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
}
