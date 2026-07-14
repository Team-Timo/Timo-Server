package com.Timo.Timo.domain.calendar.service;

import com.Timo.Timo.domain.calendar.client.GoogleOAuthClient;
import com.Timo.Timo.domain.calendar.dto.client.CalendarEventItem;
import com.Timo.Timo.domain.calendar.dto.response.CalendarDayResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarEventResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarEventsResponse;
import com.Timo.Timo.domain.calendar.entity.CalendarConnection;
import com.Timo.Timo.domain.calendar.enums.CalendarFilter;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.calendar.repository.CalendarConnectionRepository;
import com.Timo.Timo.domain.calendar.utils.CalendarEventDateResolver;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarEventQueryService {

  private final UserRepository userRepository;
  private final CalendarConnectionRepository calendarConnectionRepository;
  private final CalendarTokenService calendarTokenService;
  private final GoogleOAuthClient googleOAuthClient;

  public CalendarEventsResponse getEvents(Long userId, String filterValue, String baseDateValue) {
    User user = getUser(userId);
    ZoneId userZone = ZoneId.of(user.getZoneId());

    CalendarFilter filter = CalendarFilter.from(filterValue);
    LocalDate baseDate = parseBaseDate(baseDateValue, userZone);
    LocalDate from = filter.rangeStart(baseDate);
    LocalDate to = filter.rangeEnd(baseDate);

    CalendarConnection connection = calendarConnectionRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(CalendarErrorCode.CALENDAR_NOT_CONNECTED));
    String accessToken = calendarTokenService.ensureValidAccessToken(connection);

    Instant timeMin = from.atStartOfDay(userZone).toInstant();
    Instant timeMax = to.plusDays(1).atStartOfDay(userZone).toInstant();
    List<CalendarEventItem> items = googleOAuthClient.fetchEvents(accessToken, timeMin, timeMax);

    Map<LocalDate, List<CalendarEventResponse>> eventsByDate = groupByDate(items, from, to, userZone);

    List<CalendarDayResponse> days = from.datesUntil(to.plusDays(1))
        .map(date -> CalendarDayResponse.of(date, eventsByDate.getOrDefault(date, List.of())))
        .toList();

    return CalendarEventsResponse.of(filter, baseDate, days);
  }

  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
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

  private Map<LocalDate, List<CalendarEventResponse>> groupByDate(
      List<CalendarEventItem> items, LocalDate from, LocalDate to, ZoneId userZone
  ) {
    Map<LocalDate, List<CalendarEventResponse>> eventsByDate = new LinkedHashMap<>();

    for (CalendarEventItem item : items) {
      for (LocalDate date : CalendarEventDateResolver.resolveDates(item, userZone)) {
        if (date.isBefore(from) || date.isAfter(to)) {
          continue;
        }
        eventsByDate
            .computeIfAbsent(date, key -> new ArrayList<>())
            .add(CalendarEventResponse.of(item.summary()));
      }
    }
    return eventsByDate;
  }
}
