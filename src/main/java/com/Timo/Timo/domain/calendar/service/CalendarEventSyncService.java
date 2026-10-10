package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.client.GoogleOAuthClient;
import com.Timo.Timo.domain.calendar.dto.client.CalendarEventItem;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.home.enums.HomeFilter;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalendarEventSyncService {

  private final UserRepository userRepository;
  private final CalendarTokenService calendarTokenService;
  private final GoogleOAuthClient googleOAuthClient;
  private final CalendarTodoSynchronizer calendarTodoSynchronizer;

  public void syncEvents(Long userId, String filterValue, String baseDateValue) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    ZoneId userZone = ZoneId.of(user.getZoneId());

    HomeFilter filter = parseFilter(filterValue);
    LocalDate baseDate = parseBaseDate(baseDateValue, userZone);
    LocalDate from = filter.rangeStart(baseDate);
    LocalDate to = filter.rangeEnd(baseDate);

    String accessToken = calendarTokenService.ensureValidAccessToken(userId);

    Instant timeMin = from.atStartOfDay(userZone).toInstant();
    Instant timeMax = to.plusDays(1).atStartOfDay(userZone).toInstant();
    List<CalendarEventItem> items = googleOAuthClient.fetchEvents(accessToken, timeMin, timeMax);

    calendarTodoSynchronizer.synchronize(userId, items, from, to, userZone);
  }

  private HomeFilter parseFilter(String filterValue) {
    if (filterValue == null || filterValue.isBlank()) {
      return HomeFilter.DEFAULT;
    }
    try {
      return HomeFilter.valueOf(filterValue);
    } catch (IllegalArgumentException exception) {
      throw new CustomException(CalendarErrorCode.INVALID_FILTER_OR_DATE);
    }
  }

  private LocalDate parseBaseDate(String baseDateValue, ZoneId userZone) {
    if (baseDateValue == null || baseDateValue.isBlank()) {
      return LocalDate.now(userZone);
    }
    try {
      return LocalDate.parse(baseDateValue);
    } catch (DateTimeParseException exception) {
      throw new CustomException(CalendarErrorCode.INVALID_FILTER_OR_DATE);
    }
  }
}
