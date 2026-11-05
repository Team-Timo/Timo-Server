package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.ai.service.AiHistoryCacheService;
import com.Timo.Timo.domain.calendar.dto.event.ExternalCalendarEvent;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.calendar.utils.CalendarEventDateResolver;
import com.Timo.Timo.domain.timer.repository.TimerRecordRepository;
import com.Timo.Timo.domain.timer.service.TimerService;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.entity.TodoInstance;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import com.Timo.Timo.domain.todo.repository.SubtaskCompletionRepository;
import com.Timo.Timo.domain.todo.repository.TodoInstanceRepository;
import com.Timo.Timo.domain.todo.repository.TodoRepository;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CalendarTodoSynchronizer {

  private static final int MAX_TITLE_LENGTH = 30;
  private static final int MAX_EXTERNAL_EVENT_ID_LENGTH = 255;
  private static final int DATE_SUFFIX_LENGTH = 11;
  private static final String HASHED_ID_PREFIX = "sha256:";
  private static final String EMPTY_TITLE = "제목 없음";

  private final TodoRepository todoRepository;
  private final TodoInstanceRepository todoInstanceRepository;
  private final SubtaskCompletionRepository subtaskCompletionRepository;
  private final TimerRecordRepository timerRecordRepository;
  private final UserRepository userRepository;
  private final TimerService timerService;
  private final AiHistoryCacheService aiHistoryCacheService;

  @Transactional
  public void handleDisconnect(Long userId, CalendarSource source, LocalDate today) {
    userRepository.findByIdForUpdate(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    List<Todo> futureTodos = todoRepository.findCalendarEventsAfter(userId, source, today);
    if (!futureTodos.isEmpty()) {
      Set<Long> recordedTodoIds = new HashSet<>(timerRecordRepository.findTodoIdsHavingRecords(
          futureTodos.stream().map(Todo::getId).toList()
      ));

      List<Long> deletableTodoIds = new ArrayList<>();
      for (Todo todo : futureTodos) {
        if (recordedTodoIds.contains(todo.getId())) {
          todo.markDisconnectedFromCalendar();
          continue;
        }
        deletableTodoIds.add(todo.getId());
      }
      if (!deletableTodoIds.isEmpty()) {
        deleteTodos(deletableTodoIds);
      }
    }

    todoRepository.markDisconnectedFromCalendarUpTo(userId, source, today, LocalDateTime.now());
  }

  @Transactional
  public void synchronize(
      Long userId,
      CalendarEventProvider provider,
      List<ExternalCalendarEvent> events,
      LocalDate from,
      LocalDate to,
      ZoneId userZone
  ) {
    User user = userRepository.findByIdForUpdate(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    if (!provider.isConnected(userId)) {
      throw new CustomException(CalendarErrorCode.CALENDAR_NOT_CONNECTED);
    }

    registerEvents(user, provider.source(), events, from, to, userZone);
  }

  private void registerEvents(
      User user,
      CalendarSource source,
      List<ExternalCalendarEvent> events,
      LocalDate from,
      LocalDate to,
      ZoneId userZone
  ) {
    List<EventOccurrence> occurrences = toOccurrences(events, from, to, userZone);
    Map<String, Todo> existingByExternalEventId = findExisting(user.getId(), occurrences);

    List<EventOccurrence> newOccurrences = occurrences.stream()
        .filter(occurrence -> !existingByExternalEventId.containsKey(occurrence.externalEventId()))
        .toList();
    List<Todo> missingTodos = findMissing(user.getId(), source, occurrences, from, to);

    boolean titleChanged = false;
    Set<Long> movedTodoIds = new HashSet<>();
    Set<String> movedExternalEventIds = new HashSet<>();
    for (EventMove move : matchMoves(missingTodos, occurrences, newOccurrences)) {
      movedTodoIds.add(move.todo().getId());
      movedExternalEventIds.add(move.occurrence().externalEventId());
      if (!timerService.hasActiveTimer(move.todo().getId())) {
        titleChanged |= applyMove(move);
      }
    }

    List<EventOccurrence> remainingOccurrences = occurrences.stream()
        .filter(occurrence -> !movedExternalEventIds.contains(occurrence.externalEventId()))
        .toList();
    List<Todo> remainingMissingTodos = missingTodos.stream()
        .filter(todo -> !movedTodoIds.contains(todo.getId()))
        .toList();

    titleChanged |= saveOrUpdate(user, source, remainingOccurrences, existingByExternalEventId);
    removeMissing(remainingMissingTodos);

    if (titleChanged) {
      aiHistoryCacheService.bumpUserHistoryVersionAfterCommit(user.getId());
    }
  }

  private Map<String, Todo> findExisting(Long userId, List<EventOccurrence> occurrences) {
    if (occurrences.isEmpty()) {
      return Map.of();
    }

    List<String> externalEventIds = occurrences.stream()
        .map(EventOccurrence::externalEventId)
        .toList();
    return todoRepository.findByUser_IdAndExternalEventIdIn(userId, externalEventIds)
        .stream()
        .collect(Collectors.toMap(Todo::getExternalEventId, Function.identity()));
  }

  private List<Todo> findMissing(
      Long userId, CalendarSource source, List<EventOccurrence> occurrences, LocalDate from, LocalDate to
  ) {
    Set<String> currentExternalEventIds = occurrences.stream()
        .map(EventOccurrence::externalEventId)
        .collect(Collectors.toSet());

    return todoRepository.findCalendarEventsInRange(userId, source, from, to).stream()
        .filter(todo -> !currentExternalEventIds.contains(todo.getExternalEventId()))
        .sorted(Comparator.comparing(Todo::getStartDate))
        .toList();
  }

  private List<EventMove> matchMoves(
      List<Todo> missingTodos, List<EventOccurrence> occurrences, List<EventOccurrence> newOccurrences
  ) {
    Map<String, Long> occurrenceCountByEventId = occurrences.stream()
        .collect(Collectors.groupingBy(EventOccurrence::eventId, Collectors.counting()));

    Map<String, List<Todo>> missingByEventId = new LinkedHashMap<>();
    for (Todo todo : missingTodos) {
      String eventId = toEventId(todo.getExternalEventId());
      if (eventId != null) {
        missingByEventId.computeIfAbsent(eventId, key -> new ArrayList<>()).add(todo);
      }
    }

    Map<String, List<EventOccurrence>> newByEventId = new LinkedHashMap<>();
    for (EventOccurrence occurrence : newOccurrences) {
      newByEventId
          .computeIfAbsent(occurrence.eventId(), key -> new ArrayList<>())
          .add(occurrence);
    }

    List<EventMove> moves = new ArrayList<>();
    missingByEventId.forEach((eventId, todos) -> {
      List<EventOccurrence> candidates = newByEventId.get(eventId);
      if (candidates == null) {
        return;
      }
      boolean singleDayEvent = todos.size() == 1
          && candidates.size() == 1
          && occurrenceCountByEventId.getOrDefault(eventId, 0L) == 1L;
      if (singleDayEvent) {
        moves.add(new EventMove(todos.get(0), candidates.get(0)));
      }
    });
    return moves;
  }

  private boolean applyMove(EventMove move) {
    Todo todo = move.todo();
    EventOccurrence occurrence = move.occurrence();
    LocalDate previousDate = todo.getStartDate();

    todo.moveCalendarEvent(occurrence.date(), occurrence.externalEventId());
    timerRecordRepository.moveTargetDate(todo.getId(), previousDate, occurrence.date());
    boolean titleChanged = !todo.getTitle().equals(occurrence.title());
    if (titleChanged) {
      todo.updateFields(null, occurrence.title(), null, null, null);
    }

    if (todoInstanceRepository.findByTodo_IdAndDate(todo.getId(), occurrence.date()).isEmpty()) {
      todoInstanceRepository.findByTodo_IdAndDate(todo.getId(), previousDate)
          .ifPresent(instance -> moveInstanceToTop(instance, todo.getUser().getId(), occurrence.date()));
    }
    return titleChanged;
  }

  private void moveInstanceToTop(TodoInstance instance, Long userId, LocalDate date) {
    todoInstanceRepository.findDailyInstances(userId, date)
        .forEach(other -> other.updateSortOrder(other.getSortOrder() + 1));
    instance.moveTo(date);
    instance.updateSortOrder(0);
  }

  private String toEventId(String externalEventId) {
    if (externalEventId.startsWith(HASHED_ID_PREFIX)) {
      return null;
    }

    int separatorIndex = externalEventId.length() - DATE_SUFFIX_LENGTH;
    if (separatorIndex <= 0 || externalEventId.charAt(separatorIndex) != '_') {
      return null;
    }
    return externalEventId.substring(0, separatorIndex);
  }

  private boolean saveOrUpdate(
      User user,
      CalendarSource source,
      List<EventOccurrence> occurrences,
      Map<String, Todo> existingByExternalEventId
  ) {
    boolean titleChanged = false;
    if (occurrences.isEmpty()) {
      return titleChanged;
    }

    List<EventOccurrence> newestFirst = new ArrayList<>(occurrences);
    Collections.reverse(newestFirst);

    for (EventOccurrence occurrence : newestFirst) {
      Todo existing = existingByExternalEventId.get(occurrence.externalEventId());
      if (existing == null) {
        todoRepository.save(Todo.createFromCalendarEvent(
            user, source, occurrence.title(), occurrence.date(), occurrence.externalEventId()
        ));
        continue;
      }
      if (existing.isDeletedFromCalendar()) {
        existing.unmarkDeletedFromCalendar();
      }
      if (existing.isDisconnectedFromCalendar()) {
        existing.unmarkDisconnectedFromCalendar();
      }
      if (!existing.getTitle().equals(occurrence.title())) {
        existing.updateFields(null, occurrence.title(), null, null, null);
        titleChanged = true;
      }
    }
    return titleChanged;
  }

  private void removeMissing(List<Todo> missingTodos) {
    if (missingTodos.isEmpty()) {
      return;
    }

    Set<Long> recordedTodoIds = new HashSet<>(timerRecordRepository.findTodoIdsHavingRecords(
        missingTodos.stream().map(Todo::getId).toList()
    ));

    List<Long> deletableTodoIds = new ArrayList<>();
    for (Todo todo : missingTodos) {
      if (recordedTodoIds.contains(todo.getId())) {
        todo.markDeletedFromCalendar();
        continue;
      }
      deletableTodoIds.add(todo.getId());
    }
    if (deletableTodoIds.isEmpty()) {
      return;
    }

    deleteTodos(deletableTodoIds);
  }

  private void deleteTodos(List<Long> todoIds) {
    todoRepository.flush();
    for (Long todoId : todoIds) {
      timerService.deleteTimersByTodo(todoId);
      subtaskCompletionRepository.deleteByTodoId(todoId);
      todoInstanceRepository.deleteByTodoId(todoId);
    }
    todoRepository.deleteAllById(todoIds);
  }

  private List<EventOccurrence> toOccurrences(
      List<ExternalCalendarEvent> events, LocalDate from, LocalDate to, ZoneId userZone
  ) {
    Map<String, EventOccurrence> occurrencesByExternalEventId = new LinkedHashMap<>();

    for (ExternalCalendarEvent event : events) {
      if (event.eventId() == null) {
        continue;
      }
      String title = toTitle(event.title());
      for (LocalDate date : CalendarEventDateResolver.resolveDates(event, userZone)) {
        if (date.isBefore(from) || date.isAfter(to)) {
          continue;
        }
        String externalEventId = toExternalEventId(event.eventId(), date);
        occurrencesByExternalEventId.putIfAbsent(
            externalEventId, new EventOccurrence(event.eventId(), externalEventId, title, date)
        );
      }
    }

    return occurrencesByExternalEventId.values().stream()
        .sorted(Comparator.comparing(EventOccurrence::date))
        .toList();
  }

  private String toExternalEventId(String eventId, LocalDate date) {
    String externalEventId = eventId + "_" + date;
    if (externalEventId.length() <= MAX_EXTERNAL_EVENT_ID_LENGTH) {
      return externalEventId;
    }
    return HASHED_ID_PREFIX + sha256Hex(externalEventId);
  }

  private String sha256Hex(String value) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
          .digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
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

  private record EventOccurrence(
      String eventId, String externalEventId, String title, LocalDate date
  ) {

  }

  private record EventMove(Todo todo, EventOccurrence occurrence) {

  }
}
