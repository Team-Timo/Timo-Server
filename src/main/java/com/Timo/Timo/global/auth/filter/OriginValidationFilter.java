package com.Timo.Timo.global.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.web.filter.OncePerRequestFilter;

public class OriginValidationFilter extends OncePerRequestFilter {

  private static final List<String> PROTECTED_PATHS = List.of(
      "/api/v1/auth/reissue",
      "/api/v1/auth/logout",
      "/api/v1/auth/withdraw"
  );

  private final List<String> allowedOrigins;

  public OriginValidationFilter(List<String> allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
  ) throws ServletException, IOException {
    String path = request.getRequestURI();

    if (PROTECTED_PATHS.stream().anyMatch(path::equals)) {
      String origin = request.getHeader("Origin");
      if (origin == null || !allowedOrigins.contains(origin)) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
