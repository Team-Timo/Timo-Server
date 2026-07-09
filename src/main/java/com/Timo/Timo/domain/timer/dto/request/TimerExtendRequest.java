package com.Timo.Timo.domain.timer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TimerExtendRequest (
    @NotNull(message = "연장 시간은 필수입니다.")
    @Min(value = 1, message = "연장 시간은 1분 이상이어야 합니다.")
    @Max(value = 720, message = "연장 시간은 720분을 초과할 수 없습니다.")
    @Schema(description = "연장할 시간 (분), 1 이상", example = "10")
    Integer extendMinutes
) {}
