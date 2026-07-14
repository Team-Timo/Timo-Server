package com.Timo.Timo.domain.calendar.utils;

import com.Timo.Timo.domain.calendar.dto.client.CalendarEventItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CalendarEventDateResolver {

  public static List<LocalDate> resolveDates(CalendarEventItem item) {
    LocalDate startDate = GoogleEventDateParser.parseStart(item.start());
    LocalDate endDate = GoogleEventDateParser.parseEnd(item.end());

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
}
