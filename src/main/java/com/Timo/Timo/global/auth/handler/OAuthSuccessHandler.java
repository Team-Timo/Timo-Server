package com.Timo.Timo.global.auth.handler;

import com.Timo.Timo.global.auth.dto.CustomUserDetails;
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

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;

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
    String email = userDetails.getUser().getEmail();

    String accessToken = jwtTokenProvider.generateAccessToken(email);
    String refreshToken = jwtTokenProvider.generateRefreshToken(email);

    refreshTokenService.save(email, refreshToken);

    ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/")
        .maxAge(Duration.ofSeconds(jwtTokenProvider.getRefreshTokenExpiry()))
        .sameSite("Strict")
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/")
        .maxAge(Duration.ofSeconds(jwtTokenProvider.getRefreshTokenExpiry()))
        .sameSite("Strict")
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    getRedirectStrategy().sendRedirect(request, response, redirectUri);
  }
}
