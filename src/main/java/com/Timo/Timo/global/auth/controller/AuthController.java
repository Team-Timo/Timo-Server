package com.Timo.Timo.global.auth.controller;

import com.Timo.Timo.global.auth.docs.AuthControllerDocs;
import com.Timo.Timo.global.auth.dto.ReissueResult;
import com.Timo.Timo.global.auth.dto.request.AuthTokenRequest;
import com.Timo.Timo.global.auth.dto.response.AuthReissueResponse;
import com.Timo.Timo.global.auth.dto.response.AuthTokenResponse;
import com.Timo.Timo.global.auth.exception.AuthSuccessCode;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.auth.service.AuthService;
import com.Timo.Timo.global.auth.utils.CookieUtil;
import com.Timo.Timo.global.auth.utils.TokenExtractor;
import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
  private final JwtTokenProvider jwtTokenProvider;

  @Value("${app.auth.cookie-secure}")
  private boolean cookieSecure;

  @Override
  @PostMapping("/token")
  public ResponseEntity<BaseResponse<AuthTokenResponse>> token(
      @RequestBody AuthTokenRequest request
  ) {
    AuthTokenResponse authTokenResponse = authService.exchangeCodeForToken(request.code());

    return ResponseEntity.ok()
        .header("Cache-Control", "no-store")
        .body(BaseResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, authTokenResponse));
  }

  @Override
  @PostMapping("/reissue")
  public ResponseEntity<BaseResponse<AuthReissueResponse>> reissue(
      @CookieValue(name = "refreshToken", required=false) String refreshToken,
      @CookieValue(name = "sessionId", required = false) String sessionId
  ) {
    ReissueResult result = authService.reissue(refreshToken, sessionId);
    AuthReissueResponse authReissueResponse = AuthReissueResponse.builder()
        .accessToken(result.getAccessToken())
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE,
            CookieUtil.createCookie("refreshToken", result.getRefreshToken(),
                jwtTokenProvider.getRefreshTokenExpiry(), cookieSecure).toString())
        .header(HttpHeaders.SET_COOKIE,
            CookieUtil.createCookie("sessionId", result.getSessionId(),
                jwtTokenProvider.getRefreshTokenExpiry(), cookieSecure).toString())
        .header("Cache-Control", "no-store")
        .body(BaseResponse.onSuccess(AuthSuccessCode.REISSUE_SUCCESS, authReissueResponse));
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

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE,
            CookieUtil.expireCookie("refreshToken", cookieSecure).toString())
        .header(HttpHeaders.SET_COOKIE,
            CookieUtil.expireCookie("sessionId", cookieSecure).toString())
        .body(BaseResponse.onSuccess(AuthSuccessCode.LOGOUT_SUCCESS, null));
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

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE,
            CookieUtil.expireCookie("refreshToken", cookieSecure).toString())
        .header(HttpHeaders.SET_COOKIE,
            CookieUtil.expireCookie("sessionId", cookieSecure).toString())
        .body(BaseResponse.onSuccess(AuthSuccessCode.WITHDRAW_SUCCESS, null));
  }
}
