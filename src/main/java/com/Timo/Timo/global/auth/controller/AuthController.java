package com.Timo.Timo.global.auth.controller;

import com.Timo.Timo.global.auth.docs.AuthControllerDocs;
import com.Timo.Timo.global.auth.dto.ReissueResult;
import com.Timo.Timo.global.auth.dto.request.AuthTokenRequest;
import com.Timo.Timo.global.auth.dto.response.AuthReissueResponse;
import com.Timo.Timo.global.auth.dto.response.AuthTokenResponse;
import com.Timo.Timo.global.auth.factory.AuthResponseFactory;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.auth.service.AuthService;
import com.Timo.Timo.global.auth.utils.TokenExtractor;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

  private final AuthService authService;
  private final AuthResponseFactory authResponseFactory;

  @Operation(summary = "AccessToken 발급", description = "1회성 code로 AccessToken을 발급합니다.")
  @SecurityRequirements
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공"),
      @ApiResponse(responseCode = "400", description = "code 누락"),
      @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 인증 코드"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @Override
  @PostMapping("/token")
  public ResponseEntity<BaseResponse<AuthTokenResponse>> token(
      @Valid @RequestBody AuthTokenRequest request
  ) {
    AuthTokenResponse authTokenResponse = authService.exchangeCodeForToken(request.code());
    return authResponseFactory.tokenResponse(authTokenResponse);
  }

  @Override
  @PostMapping("/reissue")
  public ResponseEntity<BaseResponse<AuthReissueResponse>> reissue(
      @CookieValue(name = "refreshToken", required=false) String refreshToken,
      @CookieValue(name = "sessionId", required = false) String sessionId
  ) {
    ReissueResult result = authService.reissue(refreshToken, sessionId);
    return authResponseFactory.reissueResponse(result);
  }

  @Override
  @PostMapping("/logout")
  public ResponseEntity<BaseResponse<Void>> logout(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @CookieValue(name = "sessionId", required = false) String sessionId,
      HttpServletRequest request
  ) {
    String accessToken = TokenExtractor.resolveToken(request);
    authService.logout(accessToken, userDetails.getUser().getId(), sessionId);

    return authResponseFactory.logoutResponse();
  }

  @Override
  @DeleteMapping("/withdraw")
  public ResponseEntity<BaseResponse<Void>> withdraw(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @CookieValue(name = "sessionId", required = false) String sessionId,
      HttpServletRequest request
  ) {
    String accessToken = TokenExtractor.resolveToken(request);
    authService.withdraw(accessToken, userDetails.getUser().getId(), sessionId);

    return authResponseFactory.withdrawResponse();
  }
}
