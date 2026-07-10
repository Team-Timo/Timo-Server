package com.Timo.Timo.domain.timebox.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Timo.Timo.domain.timebox.docs.TimeBoxControllerDocs;
import com.Timo.Timo.domain.timebox.dto.response.TimeBoxResponse;
import com.Timo.Timo.domain.timebox.exception.TimeBoxSuccessCode;
import com.Timo.Timo.domain.timebox.service.TimeBoxService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/time-boxes")
@RequiredArgsConstructor
@Tag(name = "Time Box", description = "타임박스 API")
public class TimeBoxController implements TimeBoxControllerDocs {

	private final TimeBoxService timeBoxService;

	@Override
	@GetMapping
	public ResponseEntity<BaseResponse<List<TimeBoxResponse>>> getTimeBoxes(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(required = false) String date
	) {
		List<TimeBoxResponse> response = timeBoxService.getTimeBoxes(
			userDetails.getUserId(),
			date
		);

		return ResponseEntity.ok(
			BaseResponse.onSuccess(TimeBoxSuccessCode.TIME_BOXES_RETRIEVED, response)
		);
	}
}