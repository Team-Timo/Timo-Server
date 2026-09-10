package com.Timo.Timo.global.auth.service;

import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
  private static final String ROTATED_PREFIX = "refresh:rotated:";
  private static final long ROTATION_GRACE_SECONDS = 5;

  private static final String ROTATE_SCRIPT = """
      local current = redis.call('GET', KEYS[1])
      if current == false then
        return 0
      end
      if current ~= ARGV[1] then
        return -1
      end
      redis.call('SET', KEYS[3], ARGV[2], 'EX', ARGV[4])
      redis.call('SET', KEYS[2], ARGV[6] .. ':' .. ARGV[3], 'EX', ARGV[5])
      redis.call('DEL', KEYS[1])
      return 1
      """;

  private final RedisScript<Long> rotateScript = new DefaultRedisScript<>(ROTATE_SCRIPT, Long.class);

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

  public Optional<String> rotateIfValid(
      String userId, String oldSessionId, String expectedRefreshToken, String newRefreshToken
  ) {
    String newSessionId = UUID.randomUUID().toString();

    List<String> keys = List.of(
        KEY_PREFIX + userId + ":" + oldSessionId,
        ROTATED_PREFIX + userId + ":" + oldSessionId,
        KEY_PREFIX + userId + ":" + newSessionId
    );

    Long result = redisTemplate.execute(
        rotateScript,
        keys,
        expectedRefreshToken,
        newRefreshToken,
        newSessionId,
        String.valueOf(jwtTokenProvider.getRefreshTokenExpiry()),
        String.valueOf(ROTATION_GRACE_SECONDS),
        sha256Hex(expectedRefreshToken)
    );

    if (result != null && result == 1L) {
      return Optional.of(newSessionId);
    }
    return Optional.empty();
  }

  public Optional<String> findRotatedSessionId(String userId, String oldSessionId, String refreshToken) {
    String stored = redisTemplate.opsForValue().get(ROTATED_PREFIX + userId + ":" + oldSessionId);
    if (stored == null) {
      return Optional.empty();
    }

    int separatorIndex = stored.indexOf(':');
    if (separatorIndex < 0) {
      return Optional.empty();
    }

    String storedDigest = stored.substring(0, separatorIndex);
    String newSessionId = stored.substring(separatorIndex + 1);

    boolean digestMatches = MessageDigest.isEqual(
        storedDigest.getBytes(StandardCharsets.UTF_8),
        sha256Hex(refreshToken).getBytes(StandardCharsets.UTF_8)
    );

    if (!digestMatches) {
      return Optional.empty();
    }

    return Optional.of(newSessionId);
  }

  private static String sha256Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
    }
  }
}
