package com.Timo.Timo.domain.calendar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;
import com.Timo.Timo.domain.calendar.dto.event.ExternalEventDateTime;
import com.Timo.Timo.domain.calendar.dto.response.CalendarDayResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarEventResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarEventsResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarSourceFailureResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.enums.Provider;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalendarEventQueryServiceTest {

  private static final Long USER_ID = 1L;

  private final UserRepository userRepository = mock(UserRepository.class);

  @Test
  @DisplayName("연동된 모든 캘린더의 일정을 날짜별로 합치고 일정마다 출처를 표시한다")
  void mergeEventsFromConnectedCalendars() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
    CalendarEventProvider google = provider(CalendarSource.GOOGLE, true,
        List.of(allDayEvent("g1", "구글 회의", date(15))));
    CalendarEventProvider apple = provider(CalendarSource.APPLE, true,
        List.of(allDayEvent("a1", "애플 약속", date(15)), allDayEvent("a2", "애플 운동", date(16))));
    CalendarEventQueryService service = new CalendarEventQueryService(
        userRepository, new CalendarEventProviders(List.of(apple, google)));

    CalendarEventsResponse response = service.getEvents(USER_ID, "DAY", "2026-10-15");

    assertThat(response.days()).hasSize(1);
    CalendarDayResponse day = response.days().get(0);
    assertThat(day.date()).isEqualTo(date(15));
    assertThat(day.events()).containsExactly(
        CalendarEventResponse.of("구글 회의", CalendarSource.GOOGLE),
        CalendarEventResponse.of("애플 약속", CalendarSource.APPLE)
    );
  }

  @Test
  @DisplayName("한 캘린더만 연동되어 있으면 그 캘린더의 일정만 조회한다")
  void onlyConnectedCalendar() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
    CalendarEventProvider google = provider(CalendarSource.GOOGLE, false,
        List.of(allDayEvent("g1", "구글 회의", date(15))));
    CalendarEventProvider apple = provider(CalendarSource.APPLE, true,
        List.of(allDayEvent("a1", "애플 약속", date(15))));
    CalendarEventQueryService service = new CalendarEventQueryService(
        userRepository, new CalendarEventProviders(List.of(google, apple)));

    CalendarEventsResponse response = service.getEvents(USER_ID, "DAY", "2026-10-15");

    assertThat(response.days().get(0).events())
        .containsExactly(CalendarEventResponse.of("애플 약속", CalendarSource.APPLE));
  }

  @Test
  @DisplayName("구글 조회가 실패해도 애플 일정은 반환하고, 실패한 구글을 응답에 담는다")
  void continueWhenGoogleFails() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
    CalendarEventProvider google = provider(CalendarSource.GOOGLE, true, List.of());
    when(google.fetchEvents(eq(USER_ID), any(), any()))
        .thenThrow(new CustomException(CalendarErrorCode.GOOGLE_CALENDAR_UPSTREAM_ERROR));
    CalendarEventProvider apple = provider(CalendarSource.APPLE, true,
        List.of(allDayEvent("a1", "애플 약속", date(15))));
    CalendarEventQueryService service = new CalendarEventQueryService(
        userRepository, new CalendarEventProviders(List.of(google, apple)));

    CalendarEventsResponse response = service.getEvents(USER_ID, "DAY", "2026-10-15");

    assertThat(response.days().get(0).events())
        .containsExactly(CalendarEventResponse.of("애플 약속", CalendarSource.APPLE));
    assertThat(response.failedSources()).containsExactly(
        CalendarSourceFailureResponse.of(CalendarSource.GOOGLE, CalendarErrorCode.GOOGLE_CALENDAR_UPSTREAM_ERROR));
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

  private ExternalCalendarEvent allDayEvent(String id, String title, LocalDate date) {
    return new ExternalCalendarEvent(
        id, title, ExternalEventDateTime.ofDate(date), ExternalEventDateTime.ofDate(date.plusDays(1)));
  }

  private User user() {
    return User.builder()
        .provider(Provider.GOOGLE)
        .providerId("provider-id")
        .name("티모")
        .email("timo@gmail.com")
        .build();
  }

  private LocalDate date(int day) {
    return LocalDate.of(2026, 10, day);
  }
}
