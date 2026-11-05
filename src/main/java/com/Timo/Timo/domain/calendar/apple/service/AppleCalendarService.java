package com.Timo.Timo.domain.calendar.apple.service;

import com.Timo.Timo.domain.calendar.apple.client.AppleCalDavClient;
import com.Timo.Timo.domain.calendar.apple.dto.request.AppleCalendarConnectRequest;
import com.Timo.Timo.domain.calendar.apple.dto.response.AppleCalendarConnectResponse;
import com.Timo.Timo.domain.calendar.apple.entity.AppleCalendarConnection;
import com.Timo.Timo.domain.calendar.apple.repository.AppleCalendarConnectionRepository;
import com.Timo.Timo.domain.calendar.dto.response.CalendarDisconnectResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppleCalendarService {

  private final AppleCalendarConnectionRepository calendarConnectionRepository;
  private final UserRepository userRepository;
  private final AppleCalDavClient appleCalDavClient;
  private final AppleCalendarConnectionCommandService commandService;

  public AppleCalendarConnectResponse connect(Long userId, AppleCalendarConnectRequest request) {
    if (calendarConnectionRepository.existsByUserId(userId)) {
      throw new CustomException(CalendarErrorCode.APPLE_CALENDAR_ALREADY_CONNECTED);
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    String appleId = request.appleId().strip();
    String appPassword = request.appPassword().strip();
    String calendarHomeUrl = appleCalDavClient.discoverCalendarHomeUrl(appleId, appPassword);

    return commandService.saveConnection(user, appleId, appPassword, calendarHomeUrl);
  }

  public CalendarDisconnectResponse disconnect(Long userId) {
    AppleCalendarConnection calendarConnection = calendarConnectionRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(CalendarErrorCode.APPLE_CALENDAR_NOT_CONNECTED));

    ZoneId userZone = ZoneId.of(calendarConnection.getUser().getZoneId());
    commandService.deleteConnection(calendarConnection, LocalDate.now(userZone));

    return CalendarDisconnectResponse.builder()
        .calendarConnected(false)
        .build();
  }
}
