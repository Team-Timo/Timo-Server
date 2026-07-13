package com.Timo.Timo.domain.user.service;

import com.Timo.Timo.domain.calendar.repository.CalendarConnectionRepository;
import jakarta.persistence.Column;
import java.time.DateTimeException;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.user.dto.request.UpdateLanguageRequest;
import com.Timo.Timo.domain.user.dto.request.UpdateTimezoneRequest;
import com.Timo.Timo.domain.user.dto.response.UserProfileResponse;
import com.Timo.Timo.domain.user.dto.response.UpdateLanguageResponse;
import com.Timo.Timo.domain.user.dto.response.UpdateTimezoneResponse;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  @Column(name = "calendar_connected", nullable = false)
  private boolean calendarConnected;

  @Column(name = "calendar_email")
  private String calendarEmail;

	private final UserRepository userRepository;
  private final CalendarConnectionRepository calendarConnectionRepository;

	public UserProfileResponse getMyProfile(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    boolean calendarConnected = calendarConnectionRepository.existsByUserId(userId);
    String calendarEmail = calendarConnectionRepository.findByUserId(userId)
        .map(connection -> connection.getCalendarEmail())
        .orElse(null);

    return UserProfileResponse.from(user, calendarConnected, calendarEmail);
  }

	@Transactional
	public UpdateLanguageResponse updateLanguage(Long userId, UpdateLanguageRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		user.updateLanguage(request.language());

		return new UpdateLanguageResponse(user.getLanguage());
	}

	@Transactional
	public UpdateTimezoneResponse updateTimezone(Long userId, UpdateTimezoneRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		String zoneId = normalizeZoneId(request.zoneId());
		user.updateZoneId(zoneId);

		return new UpdateTimezoneResponse(user.getZoneId());
	}

	private String normalizeZoneId(String zoneId) {
		try {
			return ZoneId.of(zoneId).getId();
		} catch (DateTimeException exception) {
			throw new CustomException(UserErrorCode.INVALID_TIMEZONE);
		}
	}
}
