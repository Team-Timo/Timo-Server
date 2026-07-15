package com.Timo.Timo.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateTimezoneResponse(
	@Schema(description = "동기화된 시간대 ID", example = "Asia/Seoul", requiredMode = Schema.RequiredMode.REQUIRED)
	String zoneId
) {}