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

  public String generateAccessToken(String email){
    return Jwts.builder()
        .subject(email)
        .claim("type", "access")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() +  accessTokenExpirySeconds * 1000))
        .signWith(getSigningKey())
        .compact();
  }

  public String generateRefreshToken(String email){
    return Jwts.builder()
        .subject(email)
        .claim("type", "refresh")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() +  refreshTokenExpirySeconds * 1000))
        .signWith(getSigningKey())
        .compact();
  }

  public boolean validateToken(String token){
    try {
      getClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e){
      return false;
    }
  }

  public String getEmail(String token){
    return getClaims(token).getSubject();
  }

  private Claims getClaims(String token){
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public long getRefreshTokenExpiry() {
    return refreshTokenExpirySeconds;
  }
}
