package com.Timo.Timo.domain.calendar.google.controller;

import com.Timo.Timo.domain.calendar.dto.response.CalendarDisconnectResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarSuccessCode;
import com.Timo.Timo.domain.calendar.google.docs.GoogleCalendarControllerDocs;
import com.Timo.Timo.domain.calendar.google.dto.request.GoogleCalendarConnectRequest;
import com.Timo.Timo.domain.calendar.google.dto.response.GoogleCalendarAuthorizeResponse;
import com.Timo.Timo.domain.calendar.google.dto.response.GoogleCalendarConnectResponse;
import com.Timo.Timo.domain.calendar.google.service.GoogleCalendarService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;
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
@RequestMapping("/api/v1/users/calendar/google")
@RequiredArgsConstructor
@Tag(name = "Google Calendar", description = "구글 캘린더 연동 API")
public class GoogleCalendarController implements GoogleCalendarControllerDocs {

  private final GoogleCalendarService googleCalendarService;

  @Override
  @GetMapping("/authorize")
  public ResponseEntity<BaseResponse<GoogleCalendarAuthorizeResponse>> authorize(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(required = false) String redirectOrigin
  ) {
    String url = googleCalendarService.buildAuthorizationUrl(userDetails.getUserId(), redirectOrigin);
    return ResponseEntity.ok(
        BaseResponse.onSuccess(CalendarSuccessCode.CALENDAR_AUTHORIZE_URL_ISSUED,
            new GoogleCalendarAuthorizeResponse(url))
    );
  }

  @Override
  @PostMapping
  public ResponseEntity<BaseResponse<GoogleCalendarConnectResponse>> connectCalendar(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody GoogleCalendarConnectRequest request
  ) {
    Long userId = userDetails.getUserId();
    GoogleCalendarConnectResponse response = googleCalendarService.connect(userId, request);

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
    CalendarDisconnectResponse response = googleCalendarService.disconnect(userId);

    return ResponseEntity.ok(
        BaseResponse.onSuccess(CalendarSuccessCode.CALENDAR_DISCONNECTED, response)
    );
  }
}
