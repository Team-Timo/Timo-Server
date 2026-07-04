package com.Timo.Timo.global.auth.handler;

import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.auth.service.AuthCodeService;
import com.Timo.Timo.global.auth.service.RefreshTokenService;
import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;
  private final AuthCodeService authCodeService;

  @Value("${app.oauth2.redirect-uri}")
  private String redirectUri;

  @Value("${app.auth.cookie-secure:false}")
  private boolean cookieSecure;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException {

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    Long userId = userDetails.getUser().getId();
    boolean onboardingCompleted = userDetails.getUser().isOnboardingCompleted();

    String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
    String sessionId = refreshTokenService.save(String.valueOf(userId), refreshToken);

    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/api/v1/auth")
        .maxAge(Duration.ofSeconds(jwtTokenProvider.getRefreshTokenExpiry()))
        .sameSite("Strict")
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    ResponseCookie sessionCookie = ResponseCookie.from("sessionId", sessionId)
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/api/v1/auth")
        .maxAge(Duration.ofSeconds(jwtTokenProvider.getRefreshTokenExpiry()))
        .sameSite("Strict")
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie.toString());

    String code = authCodeService.generateAndSave(
        String.valueOf(userId),
        onboardingCompleted
    );

    String redirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
        .queryParam("code", code)
        .build().toUriString();

    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }
}
