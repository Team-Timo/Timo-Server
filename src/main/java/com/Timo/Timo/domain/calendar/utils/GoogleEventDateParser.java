package com.Timo.Timo.domain.calendar.utils;

import com.Timo.Timo.domain.calendar.dto.client.GoogleEventDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GoogleEventDateParser {

  public static LocalDate parseStart(GoogleEventDateTime start, ZoneId userZone) {
    if (start == null) {
      return null;
    }
    if (start.date() != null) {
      return LocalDate.parse(start.date());
    }
    if (start.dateTime() != null) {
      return OffsetDateTime.parse(start.dateTime()).atZoneSameInstant(userZone).toLocalDate();
    }
    return null;
  }

  public static LocalDate parseEnd(GoogleEventDateTime end, ZoneId userZone) {
    if (end == null) {
      return null;
    }
    if (end.date() != null) {
      return LocalDate.parse(end.date()).minusDays(1);
    }
    if (end.dateTime() != null) {
      var endZonedDateTime = OffsetDateTime.parse(end.dateTime()).atZoneSameInstant(userZone);
      LocalDate endDate = endZonedDateTime.toLocalDate();
      if (endZonedDateTime.toLocalTime().equals(LocalTime.MIDNIGHT)) {
        return endDate.minusDays(1);
      }
      return endDate;
    }
    return null;
  }
}
