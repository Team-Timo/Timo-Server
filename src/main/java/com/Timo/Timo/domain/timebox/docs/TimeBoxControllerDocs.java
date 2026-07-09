package com.Timo.Timo.domain.timebox.docs;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.timebox.dto.response.TimeBoxResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface TimeBoxControllerDocs {

	@Operation(
		summary = "날짜별 타임박스 조회",
		description = """
			지정한 날짜의 타임라인에 표시할 타임박스 목록을 조회합니다.
			실제로 타이머가 작동한 TimerSession 단위로 반환하므로 일시정지 구간은 제외됩니다.
			타이머 재개 시 같은 timerId에 새로운 sessionId를 가진 타임박스가 생성됩니다.
			startAction과 endAction으로 시작, 일시정지, 재개, 완료 시점을 구분합니다.
			사용자 종료와 예상시간 완료는 모두 COMPLETE 액션으로 반환합니다.
			완료 액션이 있는 마지막 타임박스에만 전체 실제 수행 시간을 분 단위로 반환합니다.
			진행 중인 구간은 endedAt과 endAction이 null로 반환됩니다.
			조회 결과가 없으면 빈 배열을 반환합니다.
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "타임박스 조회 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "date 누락, 형식 오류 또는 유효하지 않은 날짜",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorDto.class)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "Access Token이 없거나 만료되었거나 유효하지 않은 경우",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorDto.class)
			)
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorDto.class)
			)
		)
	})
	ResponseEntity<BaseResponse<List<TimeBoxResponse>>> getTimeBoxes(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@Parameter(
			description = "조회 날짜",
			required = true,
			example = "2026-07-01"
		)
		String date
	);
}