package com.Timo.Timo.domain.user.dto.response;

import lombok.Builder;

@Builder
public record OnboardingResponse(
    boolean onboardingCompleted,
    boolean termsAgreed
) {}
