package com.Timo.Timo.domain.calendar.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;
import com.Timo.Timo.domain.calendar.dto.event.ExternalEventDateTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalendarEventDateResolverTest {

  private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

  @Test
  @DisplayName("종일 일정의 종료일은 다음 날로 표기되므로 하루 전까지를 일정 날짜로 본다")
  void allDayEventEndIsExclusive() {
    ExternalCalendarEvent event = event(
        ExternalEventDateTime.ofDate(date(15)),
        ExternalEventDateTime.ofDate(date(18))
    );

    assertThat(CalendarEventDateResolver.resolveDates(event, SEOUL))
        .containsExactly(date(15), date(16), date(17));
  }

  @Test
  @DisplayName("시간이 정해진 일정은 사용자 시간대 기준 날짜로 계산한다")
  void timedEventUsesUserZone() {
    ExternalCalendarEvent event = event(
        ExternalEventDateTime.ofDateTime(Instant.parse("2026-10-14T16:00:00Z")),
        ExternalEventDateTime.ofDateTime(Instant.parse("2026-10-14T17:00:00Z"))
    );

    assertThat(CalendarEventDateResolver.resolveDates(event, SEOUL)).containsExactly(date(15));
  }

  @Test
  @DisplayName("자정에 끝나는 일정은 종료 날짜를 포함하지 않는다")
  void timedEventEndingAtMidnight() {
    ExternalCalendarEvent event = event(
        ExternalEventDateTime.ofDateTime(Instant.parse("2026-10-15T13:00:00Z")),
        ExternalEventDateTime.ofDateTime(Instant.parse("2026-10-15T15:00:00Z"))
    );

    assertThat(CalendarEventDateResolver.resolveDates(event, SEOUL)).containsExactly(date(15));
  }

  @Test
  @DisplayName("시작 또는 종료 정보가 없으면 날짜를 계산하지 않는다")
  void missingDateTime() {
    ExternalCalendarEvent event = event(null, ExternalEventDateTime.ofDate(date(16)));

    assertThat(CalendarEventDateResolver.resolveDates(event, SEOUL)).isEmpty();
  }

  private ExternalCalendarEvent event(ExternalEventDateTime start, ExternalEventDateTime end) {
    return new ExternalCalendarEvent("evt1", "일정", start, end);
  }

  private LocalDate date(int day) {
    return LocalDate.of(2026, 10, day);
  }
}
