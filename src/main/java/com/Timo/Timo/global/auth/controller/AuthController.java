package com.Timo.Timo.global.auth.controller;

import com.Timo.Timo.global.auth.handler.AuthErrorResponseWriter;
import com.Timo.Timo.global.auth.service.AuthCodeService;
import com.Timo.Timo.global.exception.code.ErrorCode;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @Operation(
      summary = "AccessToken 발급",
      description = "소셜 로그인 성공 시 발급된 1회용 인증 코드(code)를 통해 AccessToken을 발급합니다. "
          + "인증 코드는 1회 사용 후 즉시 만료됩니다."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "AccessToken 발급 성공",
          content = @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(example = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiJ9...\"}")
          )
      ),
      @ApiResponse(
          responseCode = "401",
          description = "유효하지 않거나 만료된 인증 코드",
          content = @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorDto.class)
          )
      )
  })
  @PostMapping("/token")
  public void token(
      @Parameter(description = "소셜 로그인 성공 시 발급된 1회용 인증 코드", required = true)
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