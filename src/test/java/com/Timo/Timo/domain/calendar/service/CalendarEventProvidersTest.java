package com.Timo.Timo.domain.calendar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import com.Timo.Timo.global.exception.CustomException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalendarEventProvidersTest {

  private static final Long USER_ID = 1L;

  @Test
  @DisplayName("연동된 공급자만 출처 순서(구글, 애플)대로 반환한다")
  void findConnectedInSourceOrder() {
    CalendarEventProvider apple = provider(CalendarSource.APPLE, true);
    CalendarEventProvider google = provider(CalendarSource.GOOGLE, true);
    CalendarEventProviders providers = new CalendarEventProviders(List.of(apple, google));

    assertThat(providers.findConnected(USER_ID)).containsExactly(google, apple);
  }

  @Test
  @DisplayName("연동되지 않은 공급자는 제외한다")
  void excludeDisconnected() {
    CalendarEventProvider google = provider(CalendarSource.GOOGLE, false);
    CalendarEventProvider apple = provider(CalendarSource.APPLE, true);
    CalendarEventProviders providers = new CalendarEventProviders(List.of(google, apple));

    assertThat(providers.findConnected(USER_ID)).containsExactly(apple);
  }

  @Test
  @DisplayName("연동된 캘린더가 하나도 없으면 예외가 발생한다")
  void noConnectedProvider() {
    CalendarEventProviders providers = new CalendarEventProviders(
        List.of(provider(CalendarSource.GOOGLE, false)));

    assertThatThrownBy(() -> providers.findConnected(USER_ID))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(CalendarErrorCode.CALENDAR_NOT_CONNECTED);
  }

  private CalendarEventProvider provider(CalendarSource source, boolean connected) {
    CalendarEventProvider provider = mock(CalendarEventProvider.class);
    when(provider.source()).thenReturn(source);
    when(provider.isConnected(USER_ID)).thenReturn(connected);
    return provider;
  }
}
