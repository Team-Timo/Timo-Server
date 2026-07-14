package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalendarStateValidator {

  private final StringRedisTemplate redisTemplate;

  public void validateState(Long userId, String state) {
    String key = "calendar:oauth:state:" + state;
    String savedUserId = redisTemplate.opsForValue().getAndDelete(key);

    if (savedUserId == null || !savedUserId.equals(String.valueOf(userId))) {
      throw new CustomException(CalendarErrorCode.CALENDAR_STATE_MISMATCH);
    }
  }
}