package com.Timo.Timo.global.auth.utils;

import java.time.Duration;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

  public static ResponseCookie createCookie(String name, String value, long maxAgeSeconds, boolean secure) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(secure)
        .path("/auth")
        .maxAge(Duration.ofSeconds(maxAgeSeconds))
        .sameSite("Strict")
        .build();
  }

  public static ResponseCookie expireCookie(String name, boolean secure) {
    return ResponseCookie.from(name, "")
        .httpOnly(true)
        .secure(secure)
        .path("/auth")
        .maxAge(0)
        .sameSite("Strict")
        .build();
  }
}
