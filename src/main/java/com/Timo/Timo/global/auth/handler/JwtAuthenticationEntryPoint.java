package com.Timo.Timo.global.auth.handler;

import com.Timo.Timo.global.exception.code.ErrorCode;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException
  ) throws IOException {

    ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

    ErrorDto errorDto = new ErrorDto(
        LocalDateTime.now(),
        errorCode.getHttpStatus().value(),
        errorCode.getCode(),
        errorCode.getMessage(),
        request.getRequestURI()
    );

    response.setStatus(errorCode.getHttpStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(errorDto));
  }
}