package com.Timo.Timo.global.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthTokenRequest(
    @Schema(description = "로그인 성공 리다이렉트 시 쿼리스트링으로 전달받은 일회용 인가코드",
        example = "550e8400-e29b-41d4-a716-446655440000")
    String code
) {
}
