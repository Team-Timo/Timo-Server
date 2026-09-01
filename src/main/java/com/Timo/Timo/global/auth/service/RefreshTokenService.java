package com.Timo.Timo.global.auth.service;

import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RedisTemplate<String, String> redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;

  private static final String KEY_PREFIX = "refresh:";
  private static final String ROTATED_PREFIX = "refresh:rotated:";
  private static final long ROTATION_GRACE_SECONDS = 5;

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
    String pattern = KEY_PREFIX + userId + ":*";
    ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
    List<String> batch = new ArrayList<>();

    try (Cursor<String> cursor = redisTemplate.scan(options)) {
      while (cursor.hasNext()) {
        batch.add(cursor.next());
        if (batch.size() >= 100) {
          redisTemplate.delete(batch);
          batch.clear();
        }
      }
      if (!batch.isEmpty()) {
        redisTemplate.delete(batch);
      }
    }
  }

  public boolean isRefreshTokenValid(String userId, String sessionId, String refreshToken) {
    return Objects.equals(refreshToken, getRefreshToken(userId, sessionId));
  }

  public String rotateRefreshToken(String userId, String oldSessionId, String newRefreshToken) {
    String newSessionId = saveRefreshToken(userId, newRefreshToken);

    redisTemplate.opsForValue().set(
        ROTATED_PREFIX + userId + ":" + oldSessionId,
        newSessionId,
        ROTATION_GRACE_SECONDS,
        TimeUnit.SECONDS
    );

    deleteRefreshToken(userId, oldSessionId);
    return newSessionId;
  }

  public Optional<String> findRotatedSessionId(String userId, String oldSessionId) {
    return Optional.ofNullable(
        redisTemplate.opsForValue().get(ROTATED_PREFIX + userId + ":" + oldSessionId)
    );
  }
}
