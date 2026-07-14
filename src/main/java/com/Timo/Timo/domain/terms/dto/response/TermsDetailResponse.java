package com.Timo.Timo.domain.terms.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TermsDetailResponse(
	@Schema(description = "약관 타입")
	String type,

	@Schema(description = "약관 언어")
	String language,

	@Schema(description = "약관 버전")
	String version,

	@Schema(description = "약관 제목")
	String title,

	@Schema(description = "약관 전문")
	String content
) {}