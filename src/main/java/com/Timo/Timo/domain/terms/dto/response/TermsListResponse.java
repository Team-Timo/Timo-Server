package com.Timo.Timo.domain.terms.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record TermsListResponse(
	@Schema(description = "약관 목록")
	List<TermsResponse> terms
) {
	public record TermsResponse(
		@Schema(description = "약관 ID", example = "1")
		Long termsId,

		@Schema(description = "약관 타입", example = "SERVICE")
		String type,

		@Schema(description = "약관 제목", example = "서비스 이용약관")
		String title,

		@Schema(description = "약관 전문", example = "TiMO는 사용자의 일정 관리와 할 일 수행을 돕기 위한 서비스입니다...")
		String content
	) {}
}