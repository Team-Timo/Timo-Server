package com.Timo.Timo.domain.calendar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;
import com.Timo.Timo.domain.calendar.dto.response.CalendarSourceFailureResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarSyncResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.enums.Provider;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalendarEventSyncServiceTest {

  private static final Long USER_ID = 1L;

  private final UserRepository userRepository = mock(UserRepository.class);
  private final CalendarTodoSynchronizer synchronizer = mock(CalendarTodoSynchronizer.class);

  @Test
  @DisplayName("연동된 캘린더마다 일정을 가져와 각각 동기화하고, 연동되지 않은 캘린더는 호출하지 않는다")
  void syncEachConnectedCalendar() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
    List<ExternalCalendarEvent> googleEvents = List.of(mock(ExternalCalendarEvent.class));
    CalendarEventProvider google = provider(CalendarSource.GOOGLE, true, googleEvents);
    CalendarEventProvider apple = provider(CalendarSource.APPLE, false, List.of());
    CalendarEventSyncService service = new CalendarEventSyncService(
        userRepository, new CalendarEventProviders(List.of(google, apple)), synchronizer);

    CalendarSyncResponse response = service.syncEvents(USER_ID, "WEEK", "2026-10-15");

    LocalDate from = LocalDate.of(2026, 10, 15);
    LocalDate to = LocalDate.of(2026, 10, 21);
    verify(synchronizer).synchronize(USER_ID, google, googleEvents, from, to, ZoneId.of("Asia/Seoul"));
    verify(apple, never()).fetchEvents(any(), any(), any());
    verify(synchronizer, never()).synchronize(eq(USER_ID), eq(apple), any(), any(), any(), any());
    assertThat(response.failedSources()).isEmpty();
  }

  @Test
  @DisplayName("구글 동기화가 실패해도 애플은 동기화하고, 실패한 구글을 응답에 담는다")
  void continueWhenGoogleFails() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
    CalendarEventProvider google = provider(CalendarSource.GOOGLE, true, List.of());
    when(google.fetchEvents(eq(USER_ID), any(), any()))
        .thenThrow(new CustomException(CalendarErrorCode.GOOGLE_CALENDAR_AUTH_FAILED));
    List<ExternalCalendarEvent> appleEvents = List.of(mock(ExternalCalendarEvent.class));
    CalendarEventProvider apple = provider(CalendarSource.APPLE, true, appleEvents);
    CalendarEventSyncService service = new CalendarEventSyncService(
        userRepository, new CalendarEventProviders(List.of(google, apple)), synchronizer);

    CalendarSyncResponse response = service.syncEvents(USER_ID, "WEEK", "2026-10-15");

    verify(synchronizer).synchronize(eq(USER_ID), eq(apple), eq(appleEvents), any(), any(), any());
    verify(synchronizer, never()).synchronize(eq(USER_ID), eq(google), any(), any(), any(), any());
    assertThat(response.failedSources()).containsExactly(
        CalendarSourceFailureResponse.of(CalendarSource.GOOGLE, CalendarErrorCode.GOOGLE_CALENDAR_AUTH_FAILED));
  }

  private CalendarEventProvider provider(
      CalendarSource source, boolean connected, List<ExternalCalendarEvent> events
  ) {
    CalendarEventProvider provider = mock(CalendarEventProvider.class);
    when(provider.source()).thenReturn(source);
    when(provider.isConnected(USER_ID)).thenReturn(connected);
    when(provider.fetchEvents(eq(USER_ID), any(), any())).thenReturn(events);
    return provider;
  }

  private User user() {
    return User.builder()
        .provider(Provider.GOOGLE)
        .providerId("provider-id")
        .name("티모")
        .email("timo@gmail.com")
        .build();
  }
}
