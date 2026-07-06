package com.Timo.Timo.global.auth.controller;

import com.Timo.Timo.global.auth.dto.response.AuthTokenResponse;
import com.Timo.Timo.global.auth.exception.AuthSuccessCode;
import com.Timo.Timo.global.auth.service.AuthService;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "AccessToken 발급", description = "1회성 code로 AccessToken을 발급합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공"),
      @ApiResponse(responseCode = "400", description = "code 누락"),
      @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 인증 코드"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping("/token")
  public ResponseEntity<BaseResponse<AuthTokenResponse>> token(
      @RequestBody Map<String, String> body
  ) {
    String code = body.get("code");
    AuthTokenResponse authTokenResponse = authService.exchangeCodeForToken(code);

    return ResponseEntity.ok()
        .header("Cache-Control", "no-store")
        .body(BaseResponse.onSuccess(AuthSuccessCode.LOGIN, authTokenResponse));
  }
}