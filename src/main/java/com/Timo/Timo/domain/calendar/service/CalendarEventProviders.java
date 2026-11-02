package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.global.exception.CustomException;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CalendarEventProviders {

  private final List<CalendarEventProvider> providers;

  public CalendarEventProviders(List<CalendarEventProvider> providers) {
    this.providers = providers.stream()
        .sorted(Comparator.comparing(CalendarEventProvider::source))
        .toList();
  }

  public List<CalendarEventProvider> findConnected(Long userId) {
    List<CalendarEventProvider> connected = providers.stream()
        .filter(provider -> provider.isConnected(userId))
        .toList();
    if (connected.isEmpty()) {
      throw new CustomException(CalendarErrorCode.CALENDAR_NOT_CONNECTED);
    }
    return connected;
  }
}
