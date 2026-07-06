package com.Timo.Timo.global.jwt.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.access-token-expires-in-seconds}")
  private long accessTokenExpirySeconds;

  @Value("${jwt.refresh-token-expires-in-seconds}")
  private long refreshTokenExpirySeconds;

  private SecretKey getSigningKey(){
    return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(Long userId){
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("type", "access")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() +  accessTokenExpirySeconds * 1000))
        .signWith(getSigningKey())
        .compact();
  }

  public String generateRefreshToken(Long userId){
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("type", "refresh")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() +  refreshTokenExpirySeconds * 1000))
        .signWith(getSigningKey())
        .compact();
  }

  public boolean validateAccessToken(String token){
    return validateToken(token, "access");
  }

  public boolean validateRefreshToken(String token){
    return validateToken(token, "refresh");
  }

  private boolean validateToken(String token, String expectedType){
    try {
      Claims claims = getClaims(token);
      return expectedType.equals(claims.get("type", String.class));
    } catch (JwtException | IllegalArgumentException e){
      return false;
    }
  }

  public Long getUserId(String token){
    return Long.parseLong(getClaims(token).getSubject());
  }

  private Claims getClaims(String token){
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public long getAccessTokenExpiry() {
    return accessTokenExpirySeconds;
  }

  public long getRefreshTokenExpiry() {
    return refreshTokenExpirySeconds;
  }

  public long getRemainingExpiry(String token){
    Date expiration = getClaims(token).getExpiration();
    return (expiration.getTime() - System.currentTimeMillis()) / 1000;
  }
}
