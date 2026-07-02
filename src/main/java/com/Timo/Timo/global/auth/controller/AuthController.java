package com.Timo.Timo.global.auth.controller;

import com.Timo.Timo.global.auth.handler.AuthErrorResponseWriter;
import com.Timo.Timo.global.auth.service.AuthCodeService;
import com.Timo.Timo.global.exception.code.ErrorCode;
import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthCodeService authCodeService;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthErrorResponseWriter authErrorResponseWriter;
  private final ObjectMapper objectMapper;

  @GetMapping("/token")
  public void token(
      @RequestParam String code,
      HttpServletRequest request,
      HttpServletResponse response
  ) throws IOException {

    String userId = authCodeService.getAndDelete(code);

    if (userId == null) {
      authErrorResponseWriter.write(response, ErrorCode.INVALID_AUTH_CODE, request.getRequestURI());
      return;
    }

    String accessToken = jwtTokenProvider.generateAccessToken(Long.parseLong(userId));

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(
        objectMapper.writeValueAsString(Map.of("accessToken", accessToken))
    );
  }
}