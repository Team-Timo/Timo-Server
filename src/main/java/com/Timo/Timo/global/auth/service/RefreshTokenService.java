package com.Timo.Timo.global.auth.service;

import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RedisTemplate<String, String> redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;

  private static final String KEY_PREFIX = "refresh:";

  public String saveRefreshToken(String userId, String refreshToken){
    String sessionId = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set(
        KEY_PREFIX + userId + ":" + sessionId,
        refreshToken,
        jwtTokenProvider.getRefreshTokenExpiry(),
        TimeUnit.SECONDS
    );
    return sessionId;
  }

  public String getRefreshToken(String userId, String sessionId) {
    return redisTemplate.opsForValue().get(KEY_PREFIX + userId + ":" + sessionId);
  }

  public void deleteRefreshToken(String userId, String sessionId) {
    redisTemplate.delete(KEY_PREFIX + userId + ":" + sessionId);
  }

  public void deleteAllRefreshTokens(String userId) {
    Set<String> keys = redisTemplate.keys(KEY_PREFIX + userId + ":*");
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  public boolean isRefreshTokenValid(String userId, String sessionId, String refreshToken) {
    return Objects.equals(refreshToken, getRefreshToken(userId, sessionId));
  }
}
