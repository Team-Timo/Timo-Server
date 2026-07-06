package com.Timo.Timo.global.auth.controller;

import com.Timo.Timo.global.auth.dto.ReissueResult;
import com.Timo.Timo.global.auth.dto.response.AuthTokenResponse;
import com.Timo.Timo.global.auth.exception.AuthSuccessCode;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.auth.service.AuthService;
import com.Timo.Timo.global.auth.utils.CookieUtil;
import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;

  @Value("${app.auth.cookie-secure}")
  private boolean cookieSecure;

  @Operation(summary = "AccessToken 발급", description = "1회성 code로 AccessToken을 발급합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공"),
      @ApiResponse(responseCode = "400", description = "code 누락"),
      @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 인증 코드"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping("/auth/token")
  public ResponseEntity<BaseResponse<AuthTokenResponse>> token(
      @RequestBody Map<String, String> body
  ) {
    String code = body.get("code");
    AuthTokenResponse authTokenResponse = authService.exchangeCodeForToken(code);

    return ResponseEntity.ok()
        .header("Cache-Control", "no-store")
        .body(BaseResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, authTokenResponse));
  }

  @Operation(summary = "AccessToken 재발급", description = "RefreshToken으로 AccessToken을 재발급합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "재발급 성공"),
      @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping("/auth/reissue")
  public ResponseEntity<BaseResponse<Map<String, String>>> reissue(
      @CookieValue(name = "refreshToken", required=false) String refreshToken,
      @CookieValue(name = "sessionId", required = false) String sessionId
  ) {
    ReissueResult result = authService.reissue(refreshToken, sessionId);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE,
            CookieUtil.createCookie("refreshToken", result.getRefreshToken(),
                jwtTokenProvider.getRefreshTokenExpiry(), cookieSecure).toString())
        .header(HttpHeaders.SET_COOKIE,
            CookieUtil.createCookie("sessionId", result.getSessionId(),
                jwtTokenProvider.getRefreshTokenExpiry(), cookieSecure).toString())
        .header("Cache-Control", "no-store")
        .body(BaseResponse.onSuccess(AuthSuccessCode.REISSUE_SUCCESS,
            Map.of("accessToken", result.getAccessToken())));
  }

  @Operation(summary = "로그아웃", description = "현재 세션을 로그아웃합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
      @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping("/auth/logout")
  public ResponseEntity<BaseResponse<Void>> logout(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @CookieValue(name = "sessionId", required = false) String sessionId,
      HttpServletRequest request
  ) {
    String accessToken = resolveToken(request);
    authService.logout(accessToken, userDetails.getUser().getId(), sessionId);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE,
            CookieUtil.expireCookie("refreshToken", cookieSecure).toString())
        .header(HttpHeaders.SET_COOKIE,
            CookieUtil.expireCookie("sessionId", cookieSecure).toString())
        .body(BaseResponse.onSuccess(AuthSuccessCode.LOGOUT_SUCCESS, null));
  }

  @Operation(summary = "회원 탈퇴", description = "회원 탈퇴 및 모든 데이터를 영구 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
      @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })

  @DeleteMapping("/users")
  public ResponseEntity<BaseResponse<Void>> withdraw(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @CookieValue(name = "sessionId", required = false) String sessionId,
      HttpServletRequest request
  ) {
    String accessToken = resolveToken(request);
    authService.withdraw(accessToken, userDetails.getUser().getId(), sessionId);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE,
            CookieUtil.expireCookie("refreshToken", cookieSecure).toString())
        .header(HttpHeaders.SET_COOKIE,
            CookieUtil.expireCookie("sessionId", cookieSecure).toString())
        .body(BaseResponse.onSuccess(AuthSuccessCode.WITHDRAW_SUCCESS, null));
  }

  private String resolveToken(HttpServletRequest request) {
    String bearer = request.getHeader("Authorization");
    if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
      return bearer.substring(7);
    }
    return null;
  }
}