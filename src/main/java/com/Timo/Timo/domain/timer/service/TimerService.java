package com.Timo.Timo.domain.timer.service;

import com.Timo.Timo.domain.timer.dto.response.TimerFinishResponse;
import com.Timo.Timo.domain.timer.dto.response.TimerStartResponse;
import com.Timo.Timo.domain.timer.entity.TimerRecord;
import com.Timo.Timo.domain.timer.entity.TimerSession;
import com.Timo.Timo.domain.timer.enums.TimerStatus;
import com.Timo.Timo.domain.timer.exception.TimerErrorCode;
import com.Timo.Timo.domain.timer.repository.TimerRecordRepository;
import com.Timo.Timo.domain.timer.repository.TimerSessionRepository;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.entity.TodoInstance;
import com.Timo.Timo.domain.todo.exception.TodoErrorCode;
import com.Timo.Timo.domain.todo.repository.TodoInstanceRepository;
import com.Timo.Timo.domain.todo.repository.TodoRepository;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;
import com.Timo.Timo.global.exception.code.ErrorCode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimerService {

  private static final List<TimerStatus> ACTIVE_STATUS = List.of(TimerStatus.RUNNING, TimerStatus.PAUSED);

  private final TimerRecordRepository timerRecordRepository;
  private final TimerSessionRepository timerSessionRepository;
  private final TodoRepository todoRepository;
  private final UserRepository userRepository;
  private final TodoInstanceRepository todoInstanceRepository;

  @Transactional
  public TimerStartResponse startTimer(Long userId, Long todoId) {
    User user = userRepository.findByIdForUpdate(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    Todo todo = todoRepository.findById(todoId)
        .orElseThrow(() -> new CustomException(TodoErrorCode.TODO_NOT_FOUND));

    if (!todo.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.FORBIDDEN);
    }

    timerRecordRepository.findByUserIdAndStatusIn(userId, ACTIVE_STATUS)
        .ifPresent(existing -> {
          throw new CustomException(TimerErrorCode.TIMER_ALREADY_RUNNING);
        });

    LocalDateTime now = LocalDateTime.now();

    TimerRecord timerRecord = TimerRecord.builder()
        .user(user)
        .todo(todo)
        .plannedSeconds(todo.getDurationSeconds())
        .startedAt(now)
        .build();
    timerRecordRepository.save(timerRecord);

    TimerSession session = TimerSession.builder()
        .timerRecord(timerRecord)
        .startedAt(now)
        .build();
    timerSessionRepository.save(session);

    return TimerStartResponse.from(timerRecord);
  }

  public boolean hasActiveTimer(Long todoId) {
    return timerRecordRepository.existsByTodo_IdAndStatusIn(todoId, ACTIVE_STATUS);
  }

  @Transactional
  public TimerFinishResponse completeTimer(Long userId, Long timerId) {
    return finishTimer(userId, timerId, TimerStatus.COMPLETED);
  }

  @Transactional
  public TimerFinishResponse stopTimer(Long userId, Long timerId) {
    return finishTimer(userId, timerId, TimerStatus.STOPPED);
  }

  private TimerFinishResponse finishTimer(Long userId, Long timerId, TimerStatus targetStatus) {
    TimerRecord timerRecord = timerRecordRepository.findByIdForUpdate(timerId)
        .orElseThrow(() -> new CustomException(TimerErrorCode.TIMER_NOT_FOUND));

    if (!timerRecord.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.FORBIDDEN);
    }

    LocalDateTime now = LocalDateTime.now();
    int actualSeconds = calculateElapsedSeconds(timerId, now);

    timerRecord.finish(targetStatus, now, actualSeconds, null);

    TodoInstance instance = getOrCreateInstance(timerRecord.getTodo(), timerRecord.getStartedAt().toLocalDate());
    instance.stopTimer();
    instance.markCompleted();

    return TimerFinishResponse.of(timerRecord);
  }

  private int calculateElapsedSeconds(Long timerRecordId, LocalDateTime now) {
    List<TimerSession> sessions = timerSessionRepository.findByTimerRecordId(timerRecordId);
    long totalSeconds = 0;
    for (TimerSession session : sessions) {
      LocalDateTime end = session.getPausedAt() != null ? session.getPausedAt() : now;
      totalSeconds += Duration.between(session.getStartedAt(), end).getSeconds();
    }
    return (int) totalSeconds;
  }

  private TodoInstance getOrCreateInstance(Todo todo, LocalDate date) {
    return todoInstanceRepository.findByTodo_IdAndDate(todo.getId(), date)
        .orElseGet(() -> todoInstanceRepository.save(TodoInstance.of(todo, date, 0)));
  }
}