package com.Timo.Timo.domain.user.dto.response;

import com.Timo.Timo.domain.user.entity.User;

public record UserProfileResponse(
	Long id,
	String name,
	String email,
	String profileImageUrl,
	String language,
	String zoneId,
	boolean calendarConnected,
	String calendarEmail
) {

	public static UserProfileResponse from(User user) {
		return new UserProfileResponse(
			user.getId(),
			user.getName(),
			user.getEmail(),
			user.getProfileImageUrl(),
			user.getLanguage().name(),
			user.getZoneId(),
			user.isCalendarConnected(),
			user.getCalendarEmail()
		);
	}
}
