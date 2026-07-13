package com.Timo.Timo.domain.calendar.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CalendarConnectRequest(
    @NotBlank(message = "authorizationCode는 필수입니다.")
    String authorizationCode,

    @NotBlank(message = "")
    String state
) {}
