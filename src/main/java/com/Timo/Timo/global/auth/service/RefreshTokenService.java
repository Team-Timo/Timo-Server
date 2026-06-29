package com.Timo.Timo.global.auth.service;

import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
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

  public void save(String email, String refreshToken){
    redisTemplate.opsForValue().set(
        KEY_PREFIX + email,
        refreshToken,
        jwtTokenProvider.getRefreshTokenExpiry(),
        TimeUnit.MILLISECONDS
    );
  }

  public String get(String email){
    return redisTemplate.opsForValue().get(KEY_PREFIX + email);
  }

  public void delete(String email){
    redisTemplate.delete(KEY_PREFIX + email);
  }

  public boolean isValid(String email, String refreshToken){
    return refreshToken.equals(get(email));
  }
}
