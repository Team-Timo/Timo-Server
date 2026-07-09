package com.Timo.Timo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateTimezoneRequest(
	@NotBlank
	@Schema(description = "IANA 시간대 ID", example = "Asia/Seoul")
	String zoneId
) {
}
