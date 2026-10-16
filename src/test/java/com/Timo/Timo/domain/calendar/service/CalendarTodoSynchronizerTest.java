package com.Timo.Timo.domain.calendar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.Timo.Timo.domain.ai.service.AiHistoryCacheService;
import com.Timo.Timo.domain.calendar.dto.client.CalendarEventItem;
import com.Timo.Timo.domain.calendar.dto.client.GoogleEventDateTime;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.calendar.repository.CalendarConnectionRepository;
import com.Timo.Timo.domain.timer.repository.TimerRecordRepository;
import com.Timo.Timo.domain.timer.service.TimerService;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.entity.TodoInstance;
import com.Timo.Timo.domain.todo.repository.SubtaskCompletionRepository;
import com.Timo.Timo.domain.todo.repository.TodoInstanceRepository;
import com.Timo.Timo.domain.todo.repository.TodoRepository;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.enums.Provider;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CalendarTodoSynchronizerTest {

  private static final Long USER_ID = 1L;
  private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
  private static final LocalDate FROM = LocalDate.of(2026, 10, 10);
  private static final LocalDate TO = LocalDate.of(2026, 10, 20);

  @Mock
  private TodoRepository todoRepository;
  @Mock
  private TodoInstanceRepository todoInstanceRepository;
  @Mock
  private SubtaskCompletionRepository subtaskCompletionRepository;
  @Mock
  private TimerRecordRepository timerRecordRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private TimerService timerService;
  @Mock
  private CalendarConnectionRepository calendarConnectionRepository;
  @Mock
  private AiHistoryCacheService aiHistoryCacheService;

  @InjectMocks
  private CalendarTodoSynchronizer synchronizer;

  private User user;

  @BeforeEach
  void setUp() {
    user = User.builder()
        .provider(Provider.GOOGLE)
        .providerId("provider-id")
        .name("티모")
        .email("timo@gmail.com")
        .build();
    ReflectionTestUtils.setField(user, "id", USER_ID);

    when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(user));
    when(calendarConnectionRepository.existsByUserId(USER_ID)).thenReturn(true);
    when(todoRepository.findByUser_IdAndExternalEventIdIn(eq(USER_ID), anyCollection())).thenReturn(List.of());
    when(todoRepository.findGoogleEventsInRange(USER_ID, FROM, TO)).thenReturn(List.of());
    when(timerRecordRepository.findTodoIdsHavingRecords(anyCollection())).thenReturn(List.of());
    when(timerService.hasActiveTimer(anyLong())).thenReturn(false);
  }

  @Nested
  @DisplayName("일정 동기화")
  class Synchronize {

    @Test
    @DisplayName("캘린더 연동이 없으면 예외가 발생한다")
    void notConnected() {
      when(calendarConnectionRepository.existsByUserId(USER_ID)).thenReturn(false);

      assertThatThrownBy(() -> synchronize(List.of()))
          .isInstanceOf(CustomException.class)
          .extracting("errorCode")
          .isEqualTo(CalendarErrorCode.CALENDAR_NOT_CONNECTED);
    }

    @Test
    @DisplayName("새 일정은 날짜별 외부 일정 ID를 가진 투두로 저장된다")
    void saveNewEvent() {
      synchronize(List.of(allDayEvent("evt1", "팀 회의", date(15), date(16))));

      Todo saved = captureSavedTodos().get(0);
      assertThat(saved.getTitle()).isEqualTo("팀 회의");
      assertThat(saved.getStartDate()).isEqualTo(date(15));
      assertThat(saved.getEndDate()).isEqualTo(date(15));
      assertThat(saved.getExternalEventId()).isEqualTo("evt1_2026-10-15");
      assertThat(saved.getDurationSeconds()).isZero();
      assertThat(saved.isGoogleEvent()).isTrue();
    }

    @Test
    @DisplayName("시간이 정해진 일정은 사용자 시간대 기준 날짜로 저장된다")
    void saveTimedEventInUserZone() {
      CalendarEventItem item = new CalendarEventItem(
          "evt1", "밤 회의",
          new GoogleEventDateTime(null, "2026-10-14T16:00:00Z"),
          new GoogleEventDateTime(null, "2026-10-14T17:00:00Z")
      );

      synchronize(List.of(item));

      assertThat(captureSavedTodos().get(0).getStartDate()).isEqualTo(date(15));
    }

    @Test
    @DisplayName("여러 날에 걸친 일정은 조회 범위 안의 날짜마다 투두로 저장된다")
    void saveMultiDayEventWithinRange() {
      synchronize(List.of(allDayEvent("evt1", "여행", date(19), date(22))));

      assertThat(captureSavedTodos())
          .extracting(Todo::getExternalEventId)
          .containsExactlyInAnyOrder("evt1_2026-10-19", "evt1_2026-10-20");
    }

    @Test
    @DisplayName("제목은 30자까지만 저장되고, 제목이 없으면 '제목 없음'으로 저장된다")
    void normalizeTitle() {
      String longTitle = "가".repeat(35);

      synchronize(List.of(
          allDayEvent("evt1", longTitle, date(15), date(16)),
          allDayEvent("evt2", "  ", date(16), date(17))
      ));

      assertThat(captureSavedTodos())
          .extracting(Todo::getTitle)
          .containsExactlyInAnyOrder("가".repeat(30), "제목 없음");
    }

    @Test
    @DisplayName("이미 저장된 일정은 새로 저장하지 않고, 제목이 바뀌면 제목만 갱신한다")
    void updateExistingTitle() {
      Todo existing = googleTodo(10L, "이전 제목", date(15), "evt1_2026-10-15");
      when(todoRepository.findByUser_IdAndExternalEventIdIn(eq(USER_ID), anyCollection()))
          .thenReturn(List.of(existing));
      when(todoRepository.findGoogleEventsInRange(USER_ID, FROM, TO)).thenReturn(List.of(existing));

      synchronize(List.of(allDayEvent("evt1", "새 제목", date(15), date(16))));

      verify(todoRepository, never()).save(any());
      assertThat(existing.getTitle()).isEqualTo("새 제목");
      verify(aiHistoryCacheService).bumpUserHistoryVersionAfterCommit(USER_ID);
    }

    @Test
    @DisplayName("제목이 그대로면 AI 히스토리 캐시를 갱신하지 않는다")
    void keepSameTitle() {
      Todo existing = googleTodo(10L, "팀 회의", date(15), "evt1_2026-10-15");
      when(todoRepository.findByUser_IdAndExternalEventIdIn(eq(USER_ID), anyCollection()))
          .thenReturn(List.of(existing));
      when(todoRepository.findGoogleEventsInRange(USER_ID, FROM, TO)).thenReturn(List.of(existing));

      synchronize(List.of(allDayEvent("evt1", "팀 회의", date(15), date(16))));

      verify(aiHistoryCacheService, never()).bumpUserHistoryVersionAfterCommit(anyLong());
    }

    @Test
    @DisplayName("다시 조회된 일정은 삭제됨·연동 해제됨 표시가 해제된다")
    void unmarkReappearedEvent() {
      Todo existing = googleTodo(10L, "팀 회의", date(15), "evt1_2026-10-15");
      existing.markDeletedFromGoogle();
      existing.markDisconnectedFromGoogle();
      when(todoRepository.findByUser_IdAndExternalEventIdIn(eq(USER_ID), anyCollection()))
          .thenReturn(List.of(existing));
      when(todoRepository.findGoogleEventsInRange(USER_ID, FROM, TO)).thenReturn(List.of(existing));

      synchronize(List.of(allDayEvent("evt1", "팀 회의", date(15), date(16))));

      assertThat(existing.isDeletedFromGoogle()).isFalse();
      assertThat(existing.isDisconnectedFromGoogle()).isFalse();
    }

    @Test
    @DisplayName("캘린더에서 사라진 일정은 타이머 기록이 없으면 투두와 관련 데이터를 삭제한다")
    void deleteMissingEventWithoutRecord() {
      Todo missing = googleTodo(10L, "취소된 회의", date(15), "evt1_2026-10-15");
      when(todoRepository.findGoogleEventsInRange(USER_ID, FROM, TO)).thenReturn(List.of(missing));

      synchronize(List.of());

      verify(timerService).deleteTimersByTodo(10L);
      verify(subtaskCompletionRepository).deleteByTodoId(10L);
      verify(todoInstanceRepository).deleteByTodoId(10L);
      verify(todoRepository).deleteAllById(List.of(10L));
    }

    @Test
    @DisplayName("캘린더에서 사라진 일정은 타이머 기록이 있으면 삭제하지 않고 삭제됨으로 표시한다")
    void markMissingEventWithRecord() {
      Todo missing = googleTodo(10L, "취소된 회의", date(15), "evt1_2026-10-15");
      when(todoRepository.findGoogleEventsInRange(USER_ID, FROM, TO)).thenReturn(List.of(missing));
      when(timerRecordRepository.findTodoIdsHavingRecords(anyCollection())).thenReturn(List.of(10L));

      synchronize(List.of());

      assertThat(missing.isDeletedFromGoogle()).isTrue();
      verify(todoRepository, never()).deleteAllById(any());
    }

    @Test
    @DisplayName("날짜만 바뀐 하루짜리 일정은 기존 투두를 새 날짜로 옮긴다")
    void moveSingleDayEvent() {
      Todo moved = googleTodo(10L, "팀 회의", date(15), "evt1_2026-10-15");
      when(todoRepository.findGoogleEventsInRange(USER_ID, FROM, TO)).thenReturn(List.of(moved));
      TodoInstance instance = TodoInstance.of(moved, date(15), 3);
      TodoInstance other = TodoInstance.of(googleTodo(11L, "다른 일", date(17), "evt2_2026-10-17"), date(17), 0);
      when(todoInstanceRepository.findByTodo_IdAndDate(10L, date(17))).thenReturn(Optional.empty());
      when(todoInstanceRepository.findByTodo_IdAndDate(10L, date(15))).thenReturn(Optional.of(instance));
      when(todoInstanceRepository.findDailyInstances(USER_ID, date(17))).thenReturn(List.of(other));

      synchronize(List.of(allDayEvent("evt1", "팀 회의", date(17), date(18))));

      assertThat(moved.getStartDate()).isEqualTo(date(17));
      assertThat(moved.getEndDate()).isEqualTo(date(17));
      assertThat(moved.getExternalEventId()).isEqualTo("evt1_2026-10-17");
      verify(timerRecordRepository).moveTargetDate(10L, date(15), date(17));
      assertThat(instance.getDate()).isEqualTo(date(17));
      assertThat(instance.getSortOrder()).isZero();
      assertThat(other.getSortOrder()).isEqualTo(1);
      verify(todoRepository, never()).save(any());
      verify(todoRepository, never()).deleteAllById(any());
    }

    @Test
    @DisplayName("날짜 이동 대상 투두의 타이머가 실행 중이면 옮기지도, 새로 저장하지도, 삭제하지도 않는다")
    void skipMoveWhenTimerActive() {
      Todo moving = googleTodo(10L, "팀 회의", date(15), "evt1_2026-10-15");
      when(todoRepository.findGoogleEventsInRange(USER_ID, FROM, TO)).thenReturn(List.of(moving));
      when(timerService.hasActiveTimer(10L)).thenReturn(true);

      synchronize(List.of(allDayEvent("evt1", "팀 회의", date(17), date(18))));

      assertThat(moving.getStartDate()).isEqualTo(date(15));
      verify(timerRecordRepository, never()).moveTargetDate(anyLong(), any(), any());
      verify(todoRepository, never()).save(any());
      verify(todoRepository, never()).deleteAllById(any());
    }

    @Test
    @DisplayName("여러 날에 걸친 일정은 날짜 이동으로 처리하지 않고 새로 저장·삭제한다")
    void doNotMoveMultiDayEvent() {
      Todo previous = googleTodo(10L, "여행", date(15), "evt1_2026-10-15");
      when(todoRepository.findGoogleEventsInRange(USER_ID, FROM, TO)).thenReturn(List.of(previous));

      synchronize(List.of(allDayEvent("evt1", "여행", date(17), date(19))));

      assertThat(previous.getStartDate()).isEqualTo(date(15));
      assertThat(captureSavedTodos())
          .extracting(Todo::getExternalEventId)
          .containsExactlyInAnyOrder("evt1_2026-10-17", "evt1_2026-10-18");
      verify(todoRepository).deleteAllById(List.of(10L));
    }
  }

  @Nested
  @DisplayName("연동 해제 처리")
  class HandleDisconnect {

    @Test
    @DisplayName("오늘 이후 투두는 타이머 기록이 없으면 삭제하고, 있으면 연동 해제됨으로 표시한 뒤 오늘까지의 투두를 일괄 표시한다")
    void handleFutureTodos() {
      LocalDate today = date(15);
      Todo withRecord = googleTodo(10L, "기록 있는 일정", date(16), "evt1_2026-10-16");
      Todo withoutRecord = googleTodo(11L, "기록 없는 일정", date(17), "evt2_2026-10-17");
      when(todoRepository.findGoogleEventsAfter(USER_ID, today)).thenReturn(List.of(withRecord, withoutRecord));
      when(timerRecordRepository.findTodoIdsHavingRecords(anyCollection())).thenReturn(List.of(10L));

      synchronizer.handleDisconnect(USER_ID, today);

      assertThat(withRecord.isDisconnectedFromGoogle()).isTrue();
      verify(todoRepository).deleteAllById(List.of(11L));
      verify(todoRepository).markDisconnectedFromGoogleUpTo(eq(USER_ID), eq(today), any());
    }

    @Test
    @DisplayName("오늘 이후 투두가 없어도 오늘까지의 투두는 연동 해제됨으로 표시한다")
    void noFutureTodos() {
      LocalDate today = date(15);
      when(todoRepository.findGoogleEventsAfter(USER_ID, today)).thenReturn(List.of());

      synchronizer.handleDisconnect(USER_ID, today);

      verify(todoRepository, never()).deleteAllById(any());
      verify(todoRepository).markDisconnectedFromGoogleUpTo(eq(USER_ID), eq(today), any());
    }
  }

  private void synchronize(List<CalendarEventItem> items) {
    synchronizer.synchronize(USER_ID, items, FROM, TO, ZONE);
  }

  private List<Todo> captureSavedTodos() {
    ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
    verify(todoRepository, atLeastOnce()).save(captor.capture());
    return captor.getAllValues();
  }

  private Todo googleTodo(Long id, String title, LocalDate date, String externalEventId) {
    Todo todo = Todo.createFromGoogleEvent(user, title, date, externalEventId);
    ReflectionTestUtils.setField(todo, "id", id);
    return todo;
  }

  private CalendarEventItem allDayEvent(String id, String title, LocalDate start, LocalDate endExclusive) {
    return new CalendarEventItem(
        id, title,
        new GoogleEventDateTime(start.toString(), null),
        new GoogleEventDateTime(endExclusive.toString(), null)
    );
  }

  private LocalDate date(int day) {
    return LocalDate.of(2026, 10, day);
  }
}
