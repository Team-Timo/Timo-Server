package com.Timo.Timo.domain.user.dto.response;

import com.Timo.Timo.domain.user.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserProfileResponse(
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
	Long id,
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
	String name,
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
	String email,
	String profileImageUrl,
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
	String language,
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
	String zoneId,
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
