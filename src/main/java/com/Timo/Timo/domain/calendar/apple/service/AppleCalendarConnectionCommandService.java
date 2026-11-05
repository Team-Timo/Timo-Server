package com.Timo.Timo.domain.calendar.apple.service;

import com.Timo.Timo.domain.calendar.apple.dto.response.AppleCalendarConnectResponse;
import com.Timo.Timo.domain.calendar.apple.entity.AppleCalendarConnection;
import com.Timo.Timo.domain.calendar.apple.repository.AppleCalendarConnectionRepository;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.calendar.service.CalendarTodoSynchronizer;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import com.Timo.Timo.domain.todo.repository.TodoRepository;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.global.exception.CustomException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppleCalendarConnectionCommandService {

  private final AppleCalendarConnectionRepository calendarConnectionRepository;
  private final CalendarTodoSynchronizer calendarTodoSynchronizer;
  private final TodoRepository todoRepository;

  @Transactional
  public AppleCalendarConnectResponse saveConnection(
      User user, String appleId, String appPassword, String calendarHomeUrl
  ) {
    AppleCalendarConnection calendarConnection = AppleCalendarConnection.builder()
        .user(user)
        .appleId(appleId)
        .appPassword(appPassword)
        .calendarHomeUrl(calendarHomeUrl)
        .build();

    try {
      calendarConnectionRepository.saveAndFlush(calendarConnection);
    } catch (DataIntegrityViolationException e) {
      throw new CustomException(CalendarErrorCode.APPLE_CALENDAR_ALREADY_CONNECTED);
    }

    todoRepository.clearDisconnectedFromCalendar(user.getId(), CalendarSource.APPLE, LocalDateTime.now());

    return AppleCalendarConnectResponse.builder()
        .appleCalendarConnected(true)
        .appleCalendarId(calendarConnection.getAppleId())
        .connectedAt(resolveConnectedAt(calendarConnection, user))
        .build();
  }

  // 애플은 앱 전용 암호 폐기 API가 없어 저장된 연결 정보만 삭제한다.
  @Transactional
  public void deleteConnection(AppleCalendarConnection calendarConnection, LocalDate today) {
    calendarTodoSynchronizer.handleDisconnect(calendarConnection.getUser().getId(), CalendarSource.APPLE, today);
    calendarConnectionRepository.delete(calendarConnection);
  }

  @Transactional
  public void updateCalendarHomeUrl(Long userId, String calendarHomeUrl) {
    calendarConnectionRepository.findByUserId(userId)
        .ifPresent(connection -> connection.updateCalendarHomeUrl(calendarHomeUrl));
  }

  private String resolveConnectedAt(AppleCalendarConnection calendarConnection, User user) {
    ZoneId userZone = ZoneId.of(user.getZoneId());
    ZonedDateTime zoned = calendarConnection.getConnectedAt()
        .atZone(ZoneOffset.UTC)
        .withZoneSameInstant(userZone);
    return zoned.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }
}
