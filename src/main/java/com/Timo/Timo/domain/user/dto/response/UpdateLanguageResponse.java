package com.Timo.Timo.domain.user.dto.response;

import com.Timo.Timo.domain.user.enums.Language;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateLanguageResponse(
	@Schema(description = "변경된 서비스 언어", requiredMode = Schema.RequiredMode.REQUIRED)
	Language language
) {
}
