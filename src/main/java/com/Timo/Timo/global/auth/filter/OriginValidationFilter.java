package com.Timo.Timo.global.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class OriginValidationFilter extends OncePerRequestFilter {

  private static final List<String> PROTECTED_PATHS = List.of(
      "/api/v1/auth/reissue",
      "/api/v1/auth/logout",
      "/api/v1/auth/withdraw"
  );

  @Value("${app.oauth2.allowed-frontend-urls}")
  private List<String> allowedFrontendUrls;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
  ) throws ServletException, IOException {
    String path = request.getRequestURI();

    if (PROTECTED_PATHS.stream().anyMatch(path::equals)) {
      String origin = request.getHeader("Origin");
      if (origin == null || !allowedFrontendUrls.contains(origin)) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
