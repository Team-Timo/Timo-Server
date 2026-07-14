package com.Timo.Timo.domain.calendar.utils;

import com.Timo.Timo.domain.calendar.dto.client.GoogleEventDateTime;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GoogleEventDateParser {

  public static LocalDate parseStart(GoogleEventDateTime start) {
    if (start == null) {
      return null;
    }
    if (start.date() != null) {
      return LocalDate.parse(start.date());
    }
    if (start.dateTime() != null) {
      return OffsetDateTime.parse(start.dateTime()).toLocalDate();
    }
    return null;
  }

  public static LocalDate parseEnd(GoogleEventDateTime end) {
    if (end == null) {
      return null;
    }
    if (end.date() != null) {
      return LocalDate.parse(end.date()).minusDays(1);
    }
    if (end.dateTime() != null) {
      return OffsetDateTime.parse(end.dateTime()).toLocalDate();
    }
    return null;
  }
}
