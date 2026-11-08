package com.Timo.Timo.domain.calendar.apple.controller;

import com.Timo.Timo.domain.calendar.apple.docs.AppleCalendarControllerDocs;
import com.Timo.Timo.domain.calendar.apple.dto.request.AppleCalendarConnectRequest;
import com.Timo.Timo.domain.calendar.apple.dto.response.AppleCalendarConnectResponse;
import com.Timo.Timo.domain.calendar.apple.service.AppleCalendarService;
import com.Timo.Timo.domain.calendar.dto.response.CalendarDisconnectResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarSuccessCode;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/calendar/apple")
@RequiredArgsConstructor
@Tag(name = "Apple Calendar", description = "애플 캘린더 연동 API")
public class AppleCalendarController implements AppleCalendarControllerDocs {

  private final AppleCalendarService appleCalendarService;

  @Override
  @PostMapping
  public ResponseEntity<BaseResponse<AppleCalendarConnectResponse>> connectCalendar(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody AppleCalendarConnectRequest request
  ) {
    Long userId = userDetails.getUserId();
    AppleCalendarConnectResponse response = appleCalendarService.connect(userId, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(
        BaseResponse.onSuccess(CalendarSuccessCode.APPLE_CALENDAR_CONNECTED, response)
    );
  }

  @Override
  @DeleteMapping
  public ResponseEntity<BaseResponse<CalendarDisconnectResponse>> disconnectCalendar(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId();
    CalendarDisconnectResponse response = appleCalendarService.disconnect(userId);

    return ResponseEntity.ok(
        BaseResponse.onSuccess(CalendarSuccessCode.APPLE_CALENDAR_DISCONNECTED, response)
    );
  }
}
