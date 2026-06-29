package com.Timo.Timo.global.auth.service;

import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RedisTemplate<String, String> redisTemplate;  // Redis 저장소 접근
  private final JwtTokenProvider jwtTokenProvider;   // 토큰 만료 시간 가져오기

  private static final String KEY_PREFIX = "refresh:";

  public void save(String email, String refreshToken){
    redisTemplate.opsForValue().set(
        KEY_PREFIX + email,
        refreshToken,
        jwtTokenProvider.getRefreshTokenExpiry(),
        TimeUnit.MILLISECONDS   // Redis에서 자동 삭제됨
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
