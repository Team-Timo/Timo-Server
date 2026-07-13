package com.Timo.Timo.domain.calendar.controller;

import com.Timo.Timo.domain.calendar.docs.CalendarControllerDocs;
import com.Timo.Timo.domain.calendar.dto.request.CalendarConnectRequest;
import com.Timo.Timo.domain.calendar.dto.response.CalendarAuthorizeResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarConnectResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarDisconnectResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarSuccessCode;
import com.Timo.Timo.domain.calendar.factory.CalendarResponseFactory;
import com.Timo.Timo.domain.calendar.service.CalendarService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "구글 캘린더 연동 API")
public class CalendarController implements CalendarControllerDocs {

  private final CalendarService calendarService;
  private final CalendarResponseFactory calendarResponseFactory;

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

    return calendarResponseFactory.connectResponse(response);
  }

  @Override
  @DeleteMapping
  public ResponseEntity<BaseResponse<CalendarDisconnectResponse>> disconnectCalendar(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId();
    CalendarDisconnectResponse response = calendarService.disconnect(userId);

    return calendarResponseFactory.disconnectResponse(response);
  }
}
