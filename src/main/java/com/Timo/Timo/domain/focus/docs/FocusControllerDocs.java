package com.Timo.Timo.domain.focus.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.focus.dto.response.FocusTodoResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface FocusControllerDocs {

	@Operation(
			summary = "집중 모드 TODO 조회",
			description = """
					집중 모드에 노출할 TODO 하나를 다음 우선순위로 조회합니다.
					1. 실행/일시정지 중인 타이머가 있으면, 날짜와 무관하게 해당 TODO를 최우선으로 반환합니다.
					   (이 경우 응답의 date는 오늘이 아니라 타이머가 시작된 날짜일 수 있습니다.)
					2. 활성 타이머가 없으면 오늘 TODO 중 미완료인 최상단 TODO 하나를 반환합니다. (sortOrder가 가장 작은 미완료 TODO)
					오늘 TODO가 하나도 없으면 "오늘의 TODO가 없습니다.",
					오늘 TODO를 모두 완료했으면 "오늘의 TODO를 모두 완료했습니다." 메시지를 반환하며,
					두 경우 모두 hasTodo는 false, todo는 null입니다.
					"""
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "집중 모드 TODO 조회 성공 / 오늘의 TODO가 없거나 모두 완료한 경우",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = FocusTodoResponse.class),
							examples = {
									@ExampleObject(
											name = "집중 모드 TODO 조회 성공",
											value = """
													{
													  "status": 200,
													  "message": "집중 모드 TODO 조회 성공",
													  "data": {
													    "date": "2026-07-14",
													    "dayOfWeek": "MON",
													    "hasTodo": true,
													    "todo": {
													      "todoId": 1,
													      "icon": "📚",
													      "title": "알고리즘 문제 풀기",
													      "completed": false,
													      "durationSeconds": 1800,
													      "priority": "HIGH",
													      "tag": {
													        "tagId": 1,
													        "name": "과제"
													      },
													      "isRepeated": false,
													      "memo": "백준 골드 2문제 목표",
													      "subtasks": []
													    }
													  }
													}
													"""
									),
									@ExampleObject(
											name = "오늘의 TODO가 없는 경우",
											value = """
													{
													  "status": 200,
													  "message": "오늘의 TODO가 없습니다.",
													  "data": {
													    "date": "2026-07-14",
													    "dayOfWeek": "MON",
													    "hasTodo": false,
													    "todo": null
													  }
													}
													"""
									),
									@ExampleObject(
											name = "오늘의 TODO를 모두 완료한 경우",
											value = """
													{
													  "status": 200,
													  "message": "오늘의 TODO를 모두 완료했습니다.",
													  "data": {
													    "date": "2026-07-14",
													    "dayOfWeek": "MON",
													    "hasTodo": false,
													    "todo": null
													  }
													}
													"""
									)
							}
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
					description = "존재하지 않는 사용자인 경우",
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
	ResponseEntity<BaseResponse<FocusTodoResponse>> getFocusTodo(
			@Parameter(hidden = true) CustomUserDetails userDetails
	);
}
