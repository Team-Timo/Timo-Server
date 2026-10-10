package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.dto.client.CalendarEventItem;
import com.Timo.Timo.domain.calendar.utils.CalendarEventDateResolver;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.repository.TodoRepository;
import com.Timo.Timo.domain.user.entity.User;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CalendarTodoSynchronizer {

  private static final int MAX_TITLE_LENGTH = 30;
  private static final int MAX_EXTERNAL_EVENT_ID_LENGTH = 255;
  private static final String EMPTY_TITLE = "제목 없음";

  private final TodoRepository todoRepository;

  public void registerEvents(
      User user, List<CalendarEventItem> items, LocalDate from, LocalDate to, ZoneId userZone
  ) {
    List<EventOccurrence> occurrences = toOccurrences(items, from, to, userZone);
    if (occurrences.isEmpty()) {
      return;
    }

    List<String> externalEventIds = occurrences.stream()
        .map(EventOccurrence::externalEventId)
        .toList();
    Map<String, Todo> existingByExternalEventId = todoRepository
        .findByUser_IdAndExternalEventIdIn(user.getId(), externalEventIds)
        .stream()
        .collect(Collectors.toMap(Todo::getExternalEventId, Function.identity()));

    List<EventOccurrence> newestFirst = new ArrayList<>(occurrences);
    Collections.reverse(newestFirst);

    for (EventOccurrence occurrence : newestFirst) {
      Todo existing = existingByExternalEventId.get(occurrence.externalEventId());
      if (existing == null) {
        todoRepository.save(Todo.createFromGoogleEvent(
            user, occurrence.title(), occurrence.date(), occurrence.externalEventId()
        ));
        continue;
      }
      if (!existing.getTitle().equals(occurrence.title())) {
        existing.updateFields(null, occurrence.title(), null, null, null);
      }
    }
  }

  private List<EventOccurrence> toOccurrences(
      List<CalendarEventItem> items, LocalDate from, LocalDate to, ZoneId userZone
  ) {
    Map<String, EventOccurrence> occurrencesByExternalEventId = new LinkedHashMap<>();

    for (CalendarEventItem item : items) {
      if (item.id() == null) {
        continue;
      }
      String title = toTitle(item.summary());
      for (LocalDate date : CalendarEventDateResolver.resolveDates(item, userZone)) {
        if (date.isBefore(from) || date.isAfter(to)) {
          continue;
        }
        String externalEventId = item.id() + "_" + date;
        if (externalEventId.length() > MAX_EXTERNAL_EVENT_ID_LENGTH) {
          continue;
        }
        occurrencesByExternalEventId.putIfAbsent(
            externalEventId, new EventOccurrence(externalEventId, title, date)
        );
      }
    }

    return occurrencesByExternalEventId.values().stream()
        .sorted(Comparator.comparing(EventOccurrence::date))
        .toList();
  }

  private String toTitle(String summary) {
    if (summary == null || summary.isBlank()) {
      return EMPTY_TITLE;
    }

    String title = summary.strip();
    if (title.codePointCount(0, title.length()) <= MAX_TITLE_LENGTH) {
      return title;
    }
    return title.substring(0, title.offsetByCodePoints(0, MAX_TITLE_LENGTH));
  }

  private record EventOccurrence(String externalEventId, String title, LocalDate date) {

  }
}
