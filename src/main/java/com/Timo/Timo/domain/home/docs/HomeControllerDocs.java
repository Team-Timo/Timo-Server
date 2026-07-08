package com.Timo.Timo.domain.home.docs;

import org.springframework.http.ResponseEntity;

import com.Timo.Timo.domain.home.dto.response.HomeResponse;
import com.Timo.Timo.domain.home.dto.response.TodayResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface HomeControllerDocs {

	@Operation(
			summary = "HOME 화면 TODO 목록 조회",
			description = """
					HOME 화면의 필터 조건에 따라 일별 TODO 목록을 조회합니다.
					DEFAULT는 기준 날짜 전후 7일, WEEK는 기준 날짜부터 7일을 반환합니다.
					각 날짜의 TODO는 미완료 먼저, 같은 완료 그룹 안에서는 sortOrder 오름차순으로 정렬됩니다.
					"""
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "홈 뷰 조회 성공",
					useReturnTypeSchema = true
			),
			@ApiResponse(
					responseCode = "400",
					description = "유효하지 않은 필터 값이거나 날짜 형식이 올바르지 않은 경우",
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
	ResponseEntity<BaseResponse<HomeResponse>> getHome(
			@Parameter(hidden = true) CustomUserDetails userDetails,
			@Parameter(description = "조회 필터(DEFAULT, WEEK)", example = "DEFAULT") String filter,
			@Parameter(description = "기준 날짜(yyyy-MM-dd), 미입력 시 오늘", example = "2026-07-22") String baseDate
	);

	@Operation(
			summary = "오늘 뷰 TODO 목록 조회",
			description = """
					오늘 하루의 TODO 목록을 하위 태스크 상세 정보까지 포함해 단일 객체로 조회합니다.
					TODO는 미완료 먼저, 같은 완료 그룹 안에서는 sortOrder 오름차순으로 정렬됩니다.
					"""
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "오늘 뷰 조회 성공",
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
					responseCode = "500",
					description = "서버 내부 오류",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ErrorDto.class)
					)
			)
	})
	ResponseEntity<BaseResponse<TodayResponse>> getToday(
			@Parameter(hidden = true) CustomUserDetails userDetails
	);
}
