package com.Timo.Timo.domain.terms.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record TermsListResponse(
	@Schema(description = "약관 목록", requiredMode = Schema.RequiredMode.REQUIRED)
	List<TermsResponse> terms
) {
	public record TermsResponse(
		@Schema(description = "약관 ID", requiredMode = Schema.RequiredMode.REQUIRED)
		Long termsId,

		@Schema(description = "약관 타입", requiredMode = Schema.RequiredMode.REQUIRED)
		String type,

		@Schema(description = "약관 제목", requiredMode = Schema.RequiredMode.REQUIRED)
		String title,

		@Schema(description = "약관 전문", requiredMode = Schema.RequiredMode.REQUIRED)
		String content
	) {}
}