package com.Timo.Timo.global.auth.filter;

import com.Timo.Timo.global.auth.utils.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LegacyCookieCleanupFilter extends OncePerRequestFilter {

  private static final List<String> TARGET_PATHS = List.of(
      "/api/v1/auth/reissue",
      "/api/v1/auth/logout",
      "/api/v1/auth/withdraw"
  );

  @Value("${app.auth.cookie-secure}")
  private boolean cookieSecure;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
  ) throws ServletException, IOException {
    String path = request.getRequestURI().substring(request.getContextPath().length());

    if (cookieSecure && TARGET_PATHS.contains(path)) {
      response.addHeader(HttpHeaders.SET_COOKIE, CookieUtil.expireLegacyCookie("refreshToken").toString());
      response.addHeader(HttpHeaders.SET_COOKIE, CookieUtil.expireLegacyCookie("sessionId").toString());
    }
    filterChain.doFilter(request, response);
  }
}
