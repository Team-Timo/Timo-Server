package com.Timo.Timo.global.auth.factory;

import com.Timo.Timo.global.auth.dto.ReissueResult;
import com.Timo.Timo.global.auth.dto.response.AuthReissueResponse;
import com.Timo.Timo.global.auth.dto.response.AuthTokenResponse;
import com.Timo.Timo.global.auth.exception.AuthSuccessCode;
import com.Timo.Timo.global.auth.utils.CookieUtil;
import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import com.Timo.Timo.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthResponseFactory {

  private final JwtTokenProvider jwtTokenProvider;

  @Value("${app.auth.cookie-secure}")
  private boolean cookieSecure;

  public ResponseEntity<BaseResponse<AuthTokenResponse>> tokenResponse(AuthTokenResponse authTokenResponse) {
    return ResponseEntity.ok()
        .header("Cache-Control", "no-store")
        .body(BaseResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, authTokenResponse));
  }

  public ResponseEntity<BaseResponse<AuthReissueResponse>> reissueResponse(ReissueResult result) {
    AuthReissueResponse body = AuthReissueResponse.builder()
        .accessToken(result.getAccessToken())
        .build();

    ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie(result.getRefreshToken()))
        .header(HttpHeaders.SET_COOKIE, sessionIdCookie(result.getSessionId()))
        .header("Cache-Control", "no-store");

    addLegacyCookieCleanup(builder);

    return builder.body(BaseResponse.onSuccess(AuthSuccessCode.REISSUE_SUCCESS, body));
  }

  private void addLegacyCookieCleanup(ResponseEntity.BodyBuilder builder) {
    if (cookieSecure) {
      builder.header(HttpHeaders.SET_COOKIE, CookieUtil.expireLegacyCookie("refreshToken").toString());
      builder.header(HttpHeaders.SET_COOKIE, CookieUtil.expireLegacyCookie("sessionId").toString());
    }
  }

  public ResponseEntity<BaseResponse<Void>> logoutResponse() {
    return expiredCookieResponse(AuthSuccessCode.LOGOUT_SUCCESS);
  }

  public ResponseEntity<BaseResponse<Void>> withdrawResponse() {
    return expiredCookieResponse(AuthSuccessCode.WITHDRAW_SUCCESS);
  }

  private ResponseEntity<BaseResponse<Void>> expiredCookieResponse(AuthSuccessCode successCode) {
    ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, CookieUtil.expireCookie("refreshToken", cookieSecure).toString())
        .header(HttpHeaders.SET_COOKIE, CookieUtil.expireCookie("sessionId", cookieSecure).toString())
        .header(HttpHeaders.CACHE_CONTROL, "no-store");

    addLegacyCookieCleanup(builder);

    return builder.body(BaseResponse.onSuccess(successCode, null));
  }

  private String refreshTokenCookie(String refreshToken) {
    return CookieUtil.createCookie("refreshToken", refreshToken,
        jwtTokenProvider.getRefreshTokenExpiry(), cookieSecure).toString();
  }

  private String sessionIdCookie(String sessionId) {
    return CookieUtil.createCookie("sessionId", sessionId,
        jwtTokenProvider.getRefreshTokenExpiry(), cookieSecure).toString();
  }
}

