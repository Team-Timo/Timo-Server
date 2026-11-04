package com.Timo.Timo.domain.calendar.dto.event;

import java.time.Instant;
import java.time.LocalDate;

public record ExternalEventDateTime(
    LocalDate date,
    Instant dateTime
) {

  public static ExternalEventDateTime ofDate(LocalDate date) {
    return new ExternalEventDateTime(date, null);
  }

  public static ExternalEventDateTime ofDateTime(Instant dateTime) {
    return new ExternalEventDateTime(null, dateTime);
  }
}
