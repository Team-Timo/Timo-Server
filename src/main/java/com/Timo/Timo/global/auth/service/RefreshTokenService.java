package com.Timo.Timo.global.auth.service;

import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RedisTemplate<String, String> redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;

  private static final String KEY_PREFIX = "refresh:";

  private static final RedisScript<Long> COMPARE_AND_DELETE_SCRIPT = new DefaultRedisScript<>(
      "local stored = redis.call('GET', KEYS[1]) "
          + "if stored == ARGV[1] then "
          + "  redis.call('DEL', KEYS[1]) "
          + "  return 1 "
          + "else "
          + "  return 0 "
          + "end",
      Long.class
  );

  public String saveRefreshToken(String userId, String refreshToken){
    String sessionId = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set(
        buildKey(userId, sessionId),
        refreshToken,
        jwtTokenProvider.getRefreshTokenExpiry(),
        TimeUnit.SECONDS
    );
    return sessionId;
  }

  public void deleteRefreshToken(String userId, String sessionId) {
    redisTemplate.delete(buildKey(userId, sessionId));
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

  public boolean validateAndConsumeRefreshToken(String userId, String sessionId, String refreshToken) {
    Long result = redisTemplate.execute(
        COMPARE_AND_DELETE_SCRIPT,
        List.of(buildKey(userId, sessionId)),
        refreshToken
    );
    return Long.valueOf(1L).equals(result);
  }

  private String buildKey(String userId, String sessionId) {
    return KEY_PREFIX + userId + ":" + sessionId;
  }
}
