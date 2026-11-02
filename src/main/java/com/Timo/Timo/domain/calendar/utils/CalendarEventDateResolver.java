package com.Timo.Timo.domain.calendar.utils;

import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;
import com.Timo.Timo.domain.calendar.dto.event.ExternalEventDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CalendarEventDateResolver {

  public static List<LocalDate> resolveDates(ExternalCalendarEvent event, ZoneId userZone) {
    LocalDate startDate = resolveStart(event.start(), userZone);
    LocalDate endDate = resolveEnd(event.end(), userZone);

    if (startDate == null || endDate == null) {
      return List.of();
    }
    if (endDate.isBefore(startDate)) {
      endDate = startDate;
    }

    List<LocalDate> dates = new ArrayList<>();
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      dates.add(date);
    }
    return dates;
  }

  private static LocalDate resolveStart(ExternalEventDateTime start, ZoneId userZone) {
    if (start == null) {
      return null;
    }
    if (start.date() != null) {
      return start.date();
    }
    if (start.dateTime() != null) {
      return start.dateTime().atZone(userZone).toLocalDate();
    }
    return null;
  }

  // 종일 일정의 종료일과 자정에 끝나는 일정은 종료 시점이 다음 날로 표기되므로 하루를 뺀다.
  private static LocalDate resolveEnd(ExternalEventDateTime end, ZoneId userZone) {
    if (end == null) {
      return null;
    }
    if (end.date() != null) {
      return end.date().minusDays(1);
    }
    if (end.dateTime() != null) {
      ZonedDateTime endZonedDateTime = end.dateTime().atZone(userZone);
      LocalDate endDate = endZonedDateTime.toLocalDate();
      if (endZonedDateTime.toLocalTime().equals(LocalTime.MIDNIGHT)) {
        return endDate.minusDays(1);
      }
      return endDate;
    }
    return null;
  }
}
