package com.Timo.Timo.domain.user.controller;

import com.Timo.Timo.global.auth.service.AuthCodeService;
import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  @GetMapping("/token")
  public ResponseEntity<Map<String, String>> token(
      @RequestParam String code
  ) {
    String userId = authCodeService.getAndDelete(code);

    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String accessToken = jwtTokenProvider.generateAccessToken(Long.parseLong(userId));
    return ResponseEntity.ok(Map.of("accessToken", accessToken));
  }
}