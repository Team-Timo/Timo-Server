package com.Timo.Timo.domain.todo.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.todo.dto.request.SubtaskStatusUpdateRequest;
import com.Timo.Timo.domain.todo.dto.request.TodoCreateRequest;
import com.Timo.Timo.domain.todo.dto.request.TodoStatusUpdateRequest;
import com.Timo.Timo.domain.todo.dto.response.SubtaskStatusChangeResponse;
import com.Timo.Timo.domain.todo.dto.response.TodoCreateResponse;
import com.Timo.Timo.domain.todo.dto.response.TodoStatusChangeResponse;
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

public interface TodoControllerDocs {

	@Operation(
		summary = "TODO 생성",
		description = """
			사용자가 새로운 TODO를 생성합니다.

			아이콘, 제목, 하위 태스크, 날짜, 예상 소요 시간, 우선순위, 태그, 반복 설정, 메모를 함께 저장합니다.
			예상 소요 시간은 duration 필드에 분:초 형식으로 전달합니다. 예: 00:15
			반복 일정은 시작일 기준 최대 1년까지 생성됩니다.

			Swagger UI 오른쪽 위의 Authorize 버튼을 눌러 유효한 Access Token을 입력해야 합니다.
			"""
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

	@Operation(
		summary = "TODO 완료 상태 변경",
		description = """
			TODO의 완료 여부를 변경합니다.

			완료/미완료 상태는 날짜별로 관리되므로 date로 대상 날짜를 지정합니다. date를 생략하면 사용자 타임존 기준 오늘로 처리합니다.
			완료로 변경 시 해당 날짜 완료 그룹의 하단으로, 미완료로 변경 시 미완료 그룹의 최상단으로 정렬 순서를 재조정하여 반환합니다.
			해당 TODO에 실행 중이거나 일시정지된 타이머가 있으면 변경할 수 없습니다.

			Swagger UI 오른쪽 위의 Authorize 버튼을 눌러 유효한 Access Token을 입력해야 합니다.
			"""
	)
	@RequestBody(
		required = true,
		description = "완료 상태 변경 요청",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = TodoStatusUpdateRequest.class),
			examples = @ExampleObject(
				name = "완료 상태 변경 요청 예시",
				value = """
					{
					  "isCompleted": true,
					  "date": "2026-07-22"
					}
					"""
			)
		)
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "완료 상태 변경 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "isCompleted가 누락된 경우",
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
			description = "존재하지 않는 TODO이거나 해당 날짜에 발생하지 않는 TODO인 경우",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorDto.class)
			)
		),
		@ApiResponse(
			responseCode = "409",
			description = "타이머가 실행 중이거나 일시정지된 상태에서 변경을 시도한 경우",
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
	ResponseEntity<BaseResponse<TodoStatusChangeResponse>> changeTodoStatus(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@Parameter(description = "대상 TODO ID", example = "145") Long todoId,
		TodoStatusUpdateRequest request
	);

	@Operation(
		summary = "하위 태스크 완료 상태 변경",
		description = """
			하위 태스크의 완료 여부를 변경합니다.

			todoId로 소유한 TODO를 확인한 뒤, 해당 TODO에 속한 subtaskId의 완료 상태를 변경합니다.

			Swagger UI 오른쪽 위의 Authorize 버튼을 눌러 유효한 Access Token을 입력해야 합니다.
			"""
	)
	@RequestBody(
		required = true,
		description = "하위 태스크 완료 상태 변경 요청",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = SubtaskStatusUpdateRequest.class),
			examples = @ExampleObject(
				name = "하위 태스크 완료 상태 변경 요청 예시",
				value = """
					{
					  "isCompleted": true
					}
					"""
			)
		)
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "하위 태스크 완료 상태 변경 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "isCompleted가 누락된 경우",
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
			description = "존재하지 않는 TODO이거나 해당 TODO에 속하지 않는 하위 태스크인 경우",
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
	ResponseEntity<BaseResponse<SubtaskStatusChangeResponse>> changeSubtaskStatus(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@Parameter(description = "대상 TODO ID", example = "5") Long todoId,
		@Parameter(description = "대상 하위 태스크 ID", example = "2") Long subtaskId,
		SubtaskStatusUpdateRequest request
	);
}
