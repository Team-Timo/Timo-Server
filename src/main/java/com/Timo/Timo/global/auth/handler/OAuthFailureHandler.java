package com.Timo.Timo.global.auth.handler;

import com.Timo.Timo.global.exception.code.ErrorCode;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule());

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception
  ) throws IOException {

    ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

    ErrorDto errorDto = new ErrorDto(
        LocalDateTime.now(),
        errorCode.getHttpStatus().value(),
        errorCode.getCode(),
        errorCode.getMessage(),
        request.getRequestURI()
    );

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(errorDto));
  }
}
