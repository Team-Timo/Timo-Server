package com.Timo.Timo.domain.calendar.controller;

import com.Timo.Timo.domain.calendar.docs.CalendarControllerDocs;
import com.Timo.Timo.domain.calendar.dto.request.CalendarConnectRequest;
import com.Timo.Timo.domain.calendar.dto.response.CalendarConnectResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarDisconnectResponse;
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

  private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
  private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar.readonly email";

  private final CalendarService calendarService;
  private final CalendarResponseFactory calendarResponseFactory;

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String clientId;

  @Value("${app.calendar.redirect-uri}")
  private String redirectUri;

  @GetMapping("/authorize")
  public void authorize(HttpServletResponse response) throws IOException {
    String url = GOOGLE_AUTH_URL
        + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
        + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
        + "&response_type=code"
        + "&scope=" + URLEncoder.encode(CALENDAR_SCOPE, StandardCharsets.UTF_8)
        + "&access_type=offline"
        + "&prompt=consent";

    response.sendRedirect(url);
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
