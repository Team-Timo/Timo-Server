package com.Timo.Timo.global.auth.service;

import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import java.util.Objects;
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

  public void save(String userId, String refreshToken){
    redisTemplate.opsForValue().set(
        KEY_PREFIX + userId,
        refreshToken,
        jwtTokenProvider.getRefreshTokenExpiry(),
        TimeUnit.SECONDS
    );
  }

  public String get(String userId){
    return redisTemplate.opsForValue().get(KEY_PREFIX + userId);
  }

  public void delete(String userId){
    redisTemplate.delete(KEY_PREFIX + userId);
  }

  public boolean isValid(String userId, String refreshToken){
    return Objects.equals(refreshToken, get(userId));
  }
}
