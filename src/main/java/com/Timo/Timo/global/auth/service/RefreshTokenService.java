package com.Timo.Timo.global.auth.service;

import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import java.util.Objects;
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

  public String save(String userId, String refreshToken){
    String sessionId = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set(
        KEY_PREFIX + userId + ":" + sessionId,
        refreshToken,
        jwtTokenProvider.getRefreshTokenExpiry(),
        TimeUnit.SECONDS
    );
    return sessionId;
  }

  public String get(String userId, String sessionId) {
    return redisTemplate.opsForValue().get(KEY_PREFIX + userId + ":" + sessionId);
  }

  public void delete(String userId, String sessionId) {
    redisTemplate.delete(KEY_PREFIX + userId + ":" + sessionId);
  }

  public boolean isValid(String userId, String sessionId, String refreshToken) {
    return Objects.equals(refreshToken, get(userId, sessionId));
  }
}
