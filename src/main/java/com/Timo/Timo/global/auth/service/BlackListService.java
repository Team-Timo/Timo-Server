package com.Timo.Timo.global.auth.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlackListService {

  private final RedisTemplate<String, String> redisTemplate;

  private static final String KEY_PREFIX = "blacklist:";

  public void addToBlackList(String accessToken, long remainingExpiry){
    redisTemplate.opsForValue().set(
        KEY_PREFIX + accessToken,
        "logout",
        remainingExpiry,
        TimeUnit.MILLISECONDS
    );
  }

  public boolean isBlackListed(String accessToken){
    return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + accessToken));
  }
}
