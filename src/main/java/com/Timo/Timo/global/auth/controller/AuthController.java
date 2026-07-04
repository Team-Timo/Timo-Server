package com.Timo.Timo.global.auth.controller;

import com.Timo.Timo.global.auth.handler.AuthErrorResponseWriter;
import com.Timo.Timo.global.auth.service.AuthCodeService;
import com.Timo.Timo.global.exception.code.ErrorCode;
import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthCodeService authCodeService;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthErrorResponseWriter authErrorResponseWriter;
  private final ObjectMapper objectMapper;

  @Operation(summary = "AccessToken 발급", description = "1회성 code로 AccessToken을 발급합니다.")
  @PostMapping("/token")
  public void token(
      @RequestBody Map<String, String> body,
      HttpServletRequest request,
      HttpServletResponse response
  ) throws IOException {
    String code = body.get("code");
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