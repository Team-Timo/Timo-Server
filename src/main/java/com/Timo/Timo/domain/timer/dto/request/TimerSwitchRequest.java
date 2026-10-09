package com.Timo.Timo.domain.timer.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TimerSwitchRequest(
    @NotNull(message = "전환할 투두 ID는 필수입니다.")
    @Schema(description = "새로 타이머를 시작할 투두 ID", example = "5")
    Long todoId,
    @NotNull(message = "대상 날짜는 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "새 타이머가 상태를 반영할 대상 날짜", type = "string", example = "2026-07-20")
    LocalDate date
) {}
