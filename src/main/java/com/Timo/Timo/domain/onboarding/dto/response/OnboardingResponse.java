package com.Timo.Timo.domain.onboarding.dto.response;

import lombok.Builder;

@Builder
public record OnboardingResponse(
    boolean onboardingCompleted
) {

}
