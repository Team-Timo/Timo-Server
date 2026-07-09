package com.Timo.Timo.domain.ai.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.ai.dto.request.CreateTodoFeedbackRequest;
import com.Timo.Timo.domain.ai.dto.response.CreateTodoFeedbackResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface AiFeedbackDocs {

	@Operation(
		tags = "AI Feedback",
		summary = "AI 투두 수행 피드백 생성",
		description = """
			사용자가 수행한 투두의 예상 소요 시간과 실제 소요 시간을 비교해 짧은 피드백을 생성합니다.

			서버는 투두 ID로 투두 정보와 가장 최근 실제 소요시간을 조회합니다.
			1. 이번 태스크의 예상 소요 시간과 실제 소요 시간
			2. 현재 투두명과 비슷한 과거 투두의 실제 소요시간 기록
			3. 같은 태그의 최근 실제 소요시간 기록

			Gemini는 현재 결과 관찰, 과거 패턴 해석, 다음 행동 추천을 1~2문장으로 압축해 반환합니다.
			비슷한 투두명 기록을 우선 참고하고, 없으면 같은 태그 기록을 참고합니다.
			둘 다 없으면 이번 태스크의 연장 또는 조기 종료 여부를 기준으로 피드백합니다.
			RPM, RPD, TPM 제한을 초과하면 Gemini 호출 전 429 응답을 반환합니다.
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "AI 투두 수행 피드백 생성 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "투두 ID가 누락되었거나 형식이 올바르지 않은 경우",
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
			responseCode = "404",
			description = "투두가 존재하지 않거나 해당 사용자의 투두가 아닌 경우",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorDto.class)
			)
		),
		@ApiResponse(
			responseCode = "429",
			description = "AI 피드백 요청 횟수 제한을 초과한 경우",
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
	ResponseEntity<BaseResponse<CreateTodoFeedbackResponse>> createFeedback(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "피드백을 생성할 투두 ID",
			content = @Content(schema = @Schema(implementation = CreateTodoFeedbackRequest.class))
		)
		CreateTodoFeedbackRequest request
	);
}