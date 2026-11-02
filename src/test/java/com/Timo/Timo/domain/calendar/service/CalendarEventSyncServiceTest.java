package com.Timo.Timo.domain.calendar.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.enums.Provider;
import com.Timo.Timo.domain.user.repository.UserRepository;
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

    service.syncEvents(USER_ID, "WEEK", "2026-10-15");

    LocalDate from = LocalDate.of(2026, 10, 15);
    LocalDate to = LocalDate.of(2026, 10, 21);
    verify(synchronizer).synchronize(USER_ID, google, googleEvents, from, to, ZoneId.of("Asia/Seoul"));
    verify(apple, never()).fetchEvents(any(), any(), any());
    verify(synchronizer, never()).synchronize(eq(USER_ID), eq(apple), any(), any(), any(), any());
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
