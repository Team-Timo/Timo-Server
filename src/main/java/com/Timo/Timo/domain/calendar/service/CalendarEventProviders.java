package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.dto.response.CalendarSourceFailureResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.global.exception.CustomException;
import io.sentry.Sentry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
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

  // 한 캘린더가 실패해도 나머지 캘린더는 계속 처리하고, 실패한 캘린더는 응답으로 알린다.
  public List<CalendarSourceFailureResponse> forEachConnected(
      Long userId, Consumer<CalendarEventProvider> action
  ) {
    List<CalendarSourceFailureResponse> failures = new ArrayList<>();
    for (CalendarEventProvider provider : findConnected(userId)) {
      try {
        action.accept(provider);
      } catch (CustomException exception) {
        log.warn("캘린더 처리 실패, 다음 캘린더를 계속 처리합니다. source={}, errorCode={}",
            provider.source(), exception.getErrorCode().getCode());
        failures.add(CalendarSourceFailureResponse.of(provider.source(), exception.getErrorCode()));
      } catch (RuntimeException exception) {
        log.error("캘린더 처리 중 예상하지 못한 오류, 다음 캘린더를 계속 처리합니다. source={}",
            provider.source(), exception);
        Sentry.captureException(exception);
        failures.add(CalendarSourceFailureResponse.of(provider.source(), CalendarErrorCode.CALENDAR_INTERNAL_ERROR));
      }
    }
    return failures;
  }
}
