package com.Timo.Timo.domain.calendar.enums;

import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.global.exception.CustomException;
import java.time.LocalDate;

public enum CalendarFilter {
  DAY {
    @Override
    public LocalDate rangeStart(LocalDate baseDate) {
      return baseDate;
    }

    @Override
    public LocalDate rangeEnd(LocalDate baseDate) {
      return baseDate;
    }
  },
  WEEK {
    @Override
    public LocalDate rangeStart(LocalDate baseDate) {
      return baseDate;
    }

    @Override
    public LocalDate rangeEnd(LocalDate baseDate) {
      return baseDate.plusDays(6);
    }
  },
  TWO_WEEK {
    @Override
    public LocalDate rangeStart(LocalDate baseDate) {
      return baseDate.minusDays(7);
    }

    @Override
    public LocalDate rangeEnd(LocalDate baseDate) {
      return baseDate.plusDays(7);
    }
  };

  public abstract LocalDate rangeStart(LocalDate baseDate);

  public abstract LocalDate rangeEnd(LocalDate baseDate);

  public static CalendarFilter from(String value) {
    if (value == null || value.isBlank()) {
      throw new CustomException(CalendarErrorCode.INVALID_FILTER_OR_DATE);
    }
    try {
      return valueOf(value);
    } catch (IllegalArgumentException exception) {
      throw new CustomException(CalendarErrorCode.INVALID_FILTER_OR_DATE);
    }
  }
}