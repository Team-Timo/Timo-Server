package com.Timo.Timo.domain.todo.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.todo.dto.request.SubtaskStatusUpdateRequest;
import com.Timo.Timo.domain.todo.dto.request.TodoCreateRequest;
import com.Timo.Timo.domain.todo.dto.request.TodoReorderRequest;
import com.Timo.Timo.domain.todo.dto.request.TodoStatusUpdateRequest;
import com.Timo.Timo.domain.todo.dto.request.TodoUpdateRequest;
import com.Timo.Timo.domain.todo.dto.response.SubtaskStatusChangeResponse;
import com.Timo.Timo.domain.todo.dto.response.TodoCreateResponse;
import com.Timo.Timo.domain.todo.dto.response.TodoDetailResponse;
import com.Timo.Timo.domain.todo.dto.response.TodoReorderResponse;
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
		summary = "TODO 상세 조회",
		description = """
			todoId와 조회 기준 날짜(date)로 단일 TODO의 상세 정보를 조회합니다.

			아이콘, 제목, 완료 여부, 날짜/요일, 예상 소요 시간, 우선순위, 태그,
			반복 설정, 타이머 상태, 메모, 정렬 순서, 하위 태스크 목록을 반환합니다.

			완료 여부·타이머 상태·정렬 순서는 date에 해당하는 인스턴스 기준으로 반환되므로,
			반복 TODO의 경우 조회하려는 날짜를 date로 전달해야 합니다.

			Swagger UI 오른쪽 위의 Authorize 버튼을 눌러 유효한 Access Token을 입력해야 합니다.
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "TODO 상세 조회 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "date가 누락되었거나 날짜 형식(yyyy-MM-dd)이 올바르지 않은 경우",
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
			description = "존재하지 않는 투두이거나, 전달한 date에 해당 투두가 존재하지 않는 경우",
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
	ResponseEntity<BaseResponse<TodoDetailResponse>> getTodoDetail(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@Parameter(description = "조회할 TODO ID", example = "3") Long todoId,
		@Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2026-07-22") String date
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
		summary = "TODO 수정",
		description = """
			기존 TODO의 제목, 메모, 날짜, 태그, 우선순위, 예상 소요 시간, 아이콘, 반복 규칙, 하위 태스크를 부분 수정합니다.

			요청 body에 포함된(null이 아닌) 필드만 수정됩니다.
			예상 소요 시간은 durationSeconds 필드에 초 단위 정수로 전달합니다.
			subtasks를 전달하면 하위 태스크 목록 전체가 교체됩니다.
			subtaskId가 있으면 기존 태스크를 수정하고, null이면 신규 태스크로 추가하며, 전달되지 않은 기존 태스크는 삭제됩니다.

			Swagger UI 오른쪽 위의 Authorize 버튼을 눌러 유효한 Access Token을 입력해야 합니다.
			"""
	)
	@RequestBody(
		required = true,
		description = "TODO 수정 요청 (수정할 필드만 포함)",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = TodoUpdateRequest.class),
			examples = @ExampleObject(
				name = "TODO 수정 요청 예시",
				value = """
					{
					  "title": "티모 하이와프 작업하기 v2",
					  "priority": "VERY_HIGH",
					  "memo": "레퍼런스 정리 먼저 → API 명세",
					  "subtasks": [
					    { "subtaskId": 1, "content": "타이머 명세 초안", "completed": true },
					    { "subtaskId": null, "content": "리뷰 반영", "completed": false }
					  ]
					}
					"""
			)
		)
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "TODO 수정 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "수정할 필드가 없거나, 잘못된 enum 값, 글자 수 제한 초과, 반복 규칙 불일치 등",
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
			description = "존재하지 않는 TODO이거나, 존재하지 않는 태그 ID 또는 하위 태스크 ID를 전달한 경우",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorDto.class)
			)
		),
		@ApiResponse(
			responseCode = "409",
			description = "타이머 실행 중에 일정/소요시간 변경을 시도했거나, 변경된 일정의 특정 날짜 TODO가 최대 개수(20개)를 초과한 경우",
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
	ResponseEntity<BaseResponse<Object>> updateTodo(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@Parameter(description = "수정할 TODO ID", example = "205") Long todoId,
		TodoUpdateRequest request
	);

	@Operation(
		summary = "TODO 삭제",
		description = """
			TODO와 연결된 하위 태스크, 반복 규칙을 함께 삭제합니다.
			해당 TODO에 실행 중이거나 일시정지된 타이머가 있으면 삭제할 수 없습니다.

			Swagger UI 오른쪽 위의 Authorize 버튼을 눌러 유효한 Access Token을 입력해야 합니다.
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "TODO 삭제 성공",
			useReturnTypeSchema = true
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
			description = "존재하지 않는 TODO인 경우",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorDto.class)
			)
		),
		@ApiResponse(
			responseCode = "409",
			description = "타이머가 실행 중이거나 일시정지된 상태에서 삭제를 시도한 경우",
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
	ResponseEntity<BaseResponse<Object>> deleteTodo(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@Parameter(description = "삭제할 TODO ID", example = "205") Long todoId
	);

	@Operation(
		summary = "TODO 순서 변경",
		description = """
			드래그 앤 드롭으로 변경된 TODO의 순서를 반영합니다.

			순서 변경은 같은 날짜의 같은 완료 그룹(미완료 그룹) 내부에서만 가능합니다.
			완료된 TODO는 순서를 변경할 수 없으며, 완료/미완료 그룹 간 이동은 완료 상태 변경 API로 처리합니다.
			newIndex는 해당 날짜 미완료 그룹 내에서 0부터 시작하는 목표 위치입니다.
			date를 생략하면 사용자 타임존 기준 오늘로 처리합니다.
			서버는 대상 TODO를 새 인덱스로 옮기고 영향받는 TODO들의 정렬 순서를 재계산합니다.

			Swagger UI 오른쪽 위의 Authorize 버튼을 눌러 유효한 Access Token을 입력해야 합니다.
			"""
	)
	@RequestBody(
		required = true,
		description = "순서 변경 요청",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = TodoReorderRequest.class),
			examples = @ExampleObject(
				name = "순서 변경 요청 예시",
				value = """
					{
					  "newIndex": 2,
					  "date": "2026-07-22"
					}
					"""
			)
		)
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "TODO 순서 변경 성공",
			useReturnTypeSchema = true
		),
		@ApiResponse(
			responseCode = "400",
			description = "newIndex가 누락되었거나 음수이거나 그룹 크기를 초과하는 경우",
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
			description = "완료된 TODO의 순서를 변경하려는 경우",
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
	ResponseEntity<BaseResponse<TodoReorderResponse>> reorderTodo(
		@Parameter(hidden = true) CustomUserDetails userDetails,
		@Parameter(description = "이동할 TODO ID", example = "145") Long todoId,
		TodoReorderRequest request
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
