package com.Timo.Timo.domain.todo.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.todo.dto.request.TodoCreateRequest;
import com.Timo.Timo.domain.todo.dto.response.TodoCreateResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

public interface TodoControllerDocs {

	@Operation(
		summary = "TODO 생성",
		description = """
			사용자가 새로운 TODO를 생성합니다.

			아이콘, 제목, 하위 태스크, 날짜, 예상 소요 시간, 우선순위, 태그, 반복 설정, 메모를 함께 저장합니다.
			예상 소요 시간은 duration 필드에 분:초 형식으로 전달합니다. 예: 00:15
			반복 일정은 시작일 기준 최대 1년까지 생성됩니다.

			Swagger UI 오른쪽 위의 Authorize 버튼을 눌러 유효한 Access Token을 입력해야 합니다.
			""",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@RequestBody(
		required = true,
		description = "TODO 생성 요청",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = TodoCreateRequest.class),
			examples = @ExampleObject(
				name = "TODO 생성 요청 예시",
				value = """
					{
					  "icon": "ICON_3",
					  "title": "티모 하이와프 작업하기",
					  "subtasks": ["타이머 명세 작성", "API 연결"],
					  "date": "2026-07-22",
					  "duration": "90:00",
					  "priority": "HIGH",
					  "tagId": 3,
					  "repeatType": "WEEKLY",
					  "repeatWeekdays": ["MON", "WED"],
					  "repeatDayOfMonth": null,
					  "memo": null
					}
					"""
			)
		)
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "201",
			description = "TODO 생성 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = """
				필수 데이터 누락, 잘못된 enum 값, duration 형식 오류,
				repeatType이 WEEKLY인데 요일 미지정,
				repeatType이 MONTHLY인데 반복 날짜 미지정,
				제목/하위 태스크/메모 길이 제한 초과
				""",
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
			description = "존재하지 않는 태그 ID를 전달한 경우",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorDto.class)
			)
		),
		@ApiResponse(
			responseCode = "409",
			description = "해당 날짜의 TODO가 최대 개수 20개를 초과한 경우",
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
	ResponseEntity<BaseResponse<TodoCreateResponse>> createTodo(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		TodoCreateRequest request
	);
}
