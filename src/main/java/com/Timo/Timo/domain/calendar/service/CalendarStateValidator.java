package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalendarStateValidator {

  public static final String STATE_KEY_PREFIX = "calendar:oauth:state:";
  public static final String VALUE_DELIMITER = "|";

  private final StringRedisTemplate redisTemplate;

  public String validateState(Long userId, String state) {
    String savedValue = redisTemplate.opsForValue().getAndDelete(STATE_KEY_PREFIX + state);
    if (savedValue == null) {
      throw new CustomException(CalendarErrorCode.CALENDAR_STATE_MISMATCH);
    }

    int delimiterIndex = savedValue.indexOf(VALUE_DELIMITER);
    String savedUserId = delimiterIndex < 0 ? savedValue : savedValue.substring(0, delimiterIndex);
    if (!savedUserId.equals(String.valueOf(userId))) {
      throw new CustomException(CalendarErrorCode.CALENDAR_STATE_MISMATCH);
    }

    return delimiterIndex < 0 ? null : savedValue.substring(delimiterIndex + 1);
  }
}
