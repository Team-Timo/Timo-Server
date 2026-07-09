package com.Timo.Timo.domain.timebox.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.Timo.Timo.domain.timebox.enums.TimeBoxAction;

import io.swagger.v3.oas.annotations.media.Schema;

public record TimeBoxResponse(
	@Schema(description = "타임박스의 원본이 되는 타이머 세션 ID", example = "201")
	Long sessionId,

	@Schema(description = "타이머 기록 ID", example = "101")
	Long timerId,

	@Schema(description = "투두 ID", example = "12")
	Long todoId,

	@Schema(description = "투두명", example = "밀린 집 정리")
	String todoName,

	@Schema(description = "타임박스 표시 날짜", example = "2026-07-01")
	LocalDate date,

	@Schema(description = "해당 날짜에 표시할 시작 일시", example = "2026-07-01T11:30:00")
	LocalDateTime startedAt,

	@Schema(
		description = "시작 일시의 액션. 이전 날짜부터 이어진 구간이면 null",
		allowableValues = {"START", "RESUME"},
		nullable = true
	)
	TimeBoxAction startAction,

	@Schema(description = "해당 날짜에 표시할 종료 일시. 진행 중이면 null",
		example = "2026-07-01T13:30:00", nullable = true)
	LocalDateTime endedAt,

	@Schema(
		description = "종료 일시의 액션. 진행 중이거나 다음 날짜로 이어지면 null",
		allowableValues = {"PAUSE", "COMPLETE"},
		nullable = true
	)
	TimeBoxAction endAction,

	@Schema(
		description = "전체 실제 수행 시간(분). 완료된 마지막 타임박스에만 포함",
		example = "70",
		nullable = true
	)
	Integer actualMinutes
) {}