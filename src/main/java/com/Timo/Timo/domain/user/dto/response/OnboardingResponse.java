package com.Timo.Timo.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record OnboardingResponse(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean onboardingCompleted,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean termsAgreed
) {}
