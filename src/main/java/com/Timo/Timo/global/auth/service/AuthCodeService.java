package com.Timo.Timo.global.auth.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthCodeService {

  private final RedisTemplate<String, String> redisTemplate;

  private static final String KEY_PREFIX = "auth:code:";
  private static final long CODE_EXPIRY_SECONDS = 300L;

  public String generateAndSave(String userId, boolean onboardingCompleted) {
    String code = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set(
        KEY_PREFIX + code,
        userId + ":" + onboardingCompleted,
        CODE_EXPIRY_SECONDS,
        TimeUnit.SECONDS
    );
    return code;
  }

  public String getAndDelete(String code) {
    return redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + code);
  }
}