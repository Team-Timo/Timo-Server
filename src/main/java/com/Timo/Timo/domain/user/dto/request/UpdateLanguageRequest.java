package com.Timo.Timo.domain.user.dto.request;

import com.Timo.Timo.domain.user.enums.Language;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateLanguageRequest(
	@NotNull
	@Schema(description = "변경할 서비스 언어")
	Language language
) {
}
