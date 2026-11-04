package com.Timo.Timo.domain.calendar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.Timo.Timo.domain.calendar.dto.response.CalendarSourceFailureResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import com.Timo.Timo.global.exception.CustomException;
import java.util.ArrayList;
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

  @Test
  @DisplayName("한 캘린더가 실패해도 다음 캘린더를 계속 처리하고, 실패한 캘린더와 에러 코드를 반환한다")
  void continueAfterFailure() {
    CalendarEventProvider google = provider(CalendarSource.GOOGLE, true);
    CalendarEventProvider apple = provider(CalendarSource.APPLE, true);
    CalendarEventProviders providers = new CalendarEventProviders(List.of(google, apple));
    List<CalendarSource> processed = new ArrayList<>();

    List<CalendarSourceFailureResponse> failures = providers.forEachConnected(USER_ID, provider -> {
      if (provider.source() == CalendarSource.GOOGLE) {
        throw new CustomException(CalendarErrorCode.GOOGLE_CALENDAR_AUTH_FAILED);
      }
      processed.add(provider.source());
    });

    assertThat(processed).containsExactly(CalendarSource.APPLE);
    assertThat(failures).containsExactly(
        CalendarSourceFailureResponse.of(CalendarSource.GOOGLE, CalendarErrorCode.GOOGLE_CALENDAR_AUTH_FAILED));
  }

  @Test
  @DisplayName("예상하지 못한 오류는 서버 내부 오류 코드로 기록하고 다음 캘린더를 계속 처리한다")
  void continueAfterUnexpectedFailure() {
    CalendarEventProvider google = provider(CalendarSource.GOOGLE, true);
    CalendarEventProvider apple = provider(CalendarSource.APPLE, true);
    CalendarEventProviders providers = new CalendarEventProviders(List.of(google, apple));
    List<CalendarSource> processed = new ArrayList<>();

    List<CalendarSourceFailureResponse> failures = providers.forEachConnected(USER_ID, provider -> {
      if (provider.source() == CalendarSource.GOOGLE) {
        throw new IllegalStateException("unexpected");
      }
      processed.add(provider.source());
    });

    assertThat(processed).containsExactly(CalendarSource.APPLE);
    assertThat(failures).extracting(CalendarSourceFailureResponse::errorCode).containsExactly("CALENDAR_500");
  }

  @Test
  @DisplayName("모든 캘린더가 성공하면 실패 목록은 비어 있다")
  void noFailures() {
    CalendarEventProviders providers = new CalendarEventProviders(
        List.of(provider(CalendarSource.GOOGLE, true), provider(CalendarSource.APPLE, true)));

    assertThat(providers.forEachConnected(USER_ID, provider -> { })).isEmpty();
  }

  private CalendarEventProvider provider(CalendarSource source, boolean connected) {
    CalendarEventProvider provider = mock(CalendarEventProvider.class);
    when(provider.source()).thenReturn(source);
    when(provider.isConnected(USER_ID)).thenReturn(connected);
    return provider;
  }
}
