package com.Timo.Timo.domain.calendar.google.dto.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;
import com.Timo.Timo.domain.calendar.dto.event.ExternalEventDateTime;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GoogleCalendarEventItemTest {

  @Test
  @DisplayName("종일 일정은 날짜로, 시간이 정해진 일정은 시각으로 변환한다")
  void toExternalEvent() {
    GoogleCalendarEventItem allDay = new GoogleCalendarEventItem(
        "evt1", "여행",
        new GoogleEventDateTime("2026-10-15", null),
        new GoogleEventDateTime("2026-10-17", null)
    );
    GoogleCalendarEventItem timed = new GoogleCalendarEventItem(
        "evt2", "회의",
        new GoogleEventDateTime(null, "2026-10-15T09:00:00+09:00"),
        new GoogleEventDateTime(null, "2026-10-15T10:00:00+09:00")
    );

    assertThat(allDay.toExternalEvent()).isEqualTo(new ExternalCalendarEvent(
        "evt1", "여행",
        ExternalEventDateTime.ofDate(LocalDate.of(2026, 10, 15)),
        ExternalEventDateTime.ofDate(LocalDate.of(2026, 10, 17))
    ));
    assertThat(timed.toExternalEvent()).isEqualTo(new ExternalCalendarEvent(
        "evt2", "회의",
        ExternalEventDateTime.ofDateTime(Instant.parse("2026-10-15T00:00:00Z")),
        ExternalEventDateTime.ofDateTime(Instant.parse("2026-10-15T01:00:00Z"))
    ));
  }

  @Test
  @DisplayName("시작·종료 정보가 없으면 빈 값으로 변환한다")
  void missingDateTime() {
    GoogleCalendarEventItem item = new GoogleCalendarEventItem("evt1", "일정", null, new GoogleEventDateTime(null, null));

    ExternalCalendarEvent event = item.toExternalEvent();

    assertThat(event.start()).isNull();
    assertThat(event.end()).isNull();
  }
}
