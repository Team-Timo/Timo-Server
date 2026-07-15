package com.Timo.Timo.domain.user.dto.request;

import com.Timo.Timo.domain.user.enums.Language;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record OnboardingRequest(

    @NotNull(message = "language는 필수입니다.")
    Language language,

    @NotNull(message = "predictionAccuracy는 필수입니다.")
    @Min(value = 1, message = "predictionAccuracy는 1~4 사이여야 합니다.")
    @Max(value = 4, message = "predictionAccuracy는 1~4 사이여야 합니다.")
    Long predictionAccuracy,

    @NotNull(message = "wakeupTime은 필수입니다.")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$|^24:00$", message = "wakeUpTime 형식은 HH:MM 이어야 합니다.")
    String wakeUpTime,

    @NotNull(message = "bedTime은 필수입니다.")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$|^24:00$", message = "bedTime 형식은 HH:MM 이어야 합니다.")
    String bedTime
){}
