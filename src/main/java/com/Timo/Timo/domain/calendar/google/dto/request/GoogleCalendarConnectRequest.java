package com.Timo.Timo.domain.calendar.google.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record GoogleCalendarConnectRequest(
    @NotBlank(message = "authorizationCode는 필수입니다.")
    @Schema(description = "구글 OAuth 동의 완료 후 발급된 authorization code", requiredMode = Schema.RequiredMode.REQUIRED)
    String authorizationCode,

    @NotBlank(message = "state는 필수입니다.")
    @Schema(description = "authorize API 호출 시 발급받은 state 값 (CSRF 방어용)", requiredMode = Schema.RequiredMode.REQUIRED)
    String state
) {}
