package com.Timo.Timo.domain.calendar.google.dto.client;

import com.Timo.Timo.domain.calendar.dto.event.ExternalEventDateTime;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record GoogleEventDateTime (
    String date,
    String dateTime
) {

  public ExternalEventDateTime toExternalEventDateTime() {
    if (date != null) {
      return ExternalEventDateTime.ofDate(LocalDate.parse(date));
    }
    if (dateTime != null) {
      return ExternalEventDateTime.ofDateTime(OffsetDateTime.parse(dateTime).toInstant());
    }
    return null;
  }
}
