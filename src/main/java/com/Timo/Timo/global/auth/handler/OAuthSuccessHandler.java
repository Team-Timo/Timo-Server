package com.Timo.Timo.global.auth.handler;

import com.Timo.Timo.global.auth.filter.OAuthOriginCaptureFilter;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.auth.service.AuthCodeService;
import com.Timo.Timo.global.auth.service.RefreshTokenService;
import com.Timo.Timo.global.auth.utils.CookieUtil;
import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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

  @Value("${app.oauth2.allowed-frontend-urls}")
  private List<String> allowedFrontendUrls;

  @Value("${app.oauth2.callback-path}")
  private String callbackPath;

  @Value("${app.auth.cookie-secure}")
  private boolean cookieSecure;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException {

    String frontendOrigin = resolveFrontendOrigin(request);

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    Long userId = userDetails.getUserId();
    boolean onboardingCompleted = userDetails.getUser().isOnboardingCompleted();

    String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
    String sessionId = refreshTokenService.saveRefreshToken(String.valueOf(userId), refreshToken);

    response.addHeader(HttpHeaders.SET_COOKIE,
        CookieUtil.createCookie("refreshToken", refreshToken,
            jwtTokenProvider.getRefreshTokenExpiry(), cookieSecure).toString());
    response.addHeader(HttpHeaders.SET_COOKIE,
        CookieUtil.createCookie("sessionId", sessionId,
            jwtTokenProvider.getRefreshTokenExpiry(), cookieSecure).toString());

    if (cookieSecure) {
      response.addHeader(HttpHeaders.SET_COOKIE, CookieUtil.expireLegacyCookie("refreshToken").toString());
      response.addHeader(HttpHeaders.SET_COOKIE, CookieUtil.expireLegacyCookie("sessionId").toString());
    }

    String code = authCodeService.generateAndSave(
        String.valueOf(userId),
        onboardingCompleted
    );

    String redirectUrl = UriComponentsBuilder.fromUriString(frontendOrigin + callbackPath)
        .queryParam("code", code)
        .build().toUriString();

    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }

  private String resolveFrontendOrigin(HttpServletRequest request) {
    String origin = null;
    if (request.getSession(false) != null) {
      origin = (String) request.getSession().getAttribute(OAuthOriginCaptureFilter.SESSION_KEY);
      request.getSession().removeAttribute(OAuthOriginCaptureFilter.SESSION_KEY);
    }

    if (origin != null && allowedFrontendUrls.contains(origin)) {
      return origin;
    }
    return allowedFrontendUrls.get(0);
  }
}
