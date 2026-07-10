package com.Timo.Timo.domain.timer.dto.request;

import com.Timo.Timo.domain.timer.enums.TimerAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record TimerActionRequest (
    @NotNull(message = "action은 필수입니다.")
    @Schema(description = "타이머 동작 (PAUSE/RESUME)", example = "PAUSE")
    TimerAction action
){}
