package com.Timo.Timo.domain.focus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Timo.Timo.domain.focus.docs.FocusControllerDocs;
import com.Timo.Timo.domain.focus.dto.FocusTodoResult;
import com.Timo.Timo.domain.focus.dto.response.FocusTodoResponse;
import com.Timo.Timo.domain.focus.service.FocusService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/focus")
@RequiredArgsConstructor
@Tag(name = "Focus", description = "집중 모드 API")
public class FocusController implements FocusControllerDocs {

	private final FocusService focusService;

	@Override
	@GetMapping
	public ResponseEntity<BaseResponse<FocusTodoResponse>> getFocusTodo(
			@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		FocusTodoResult result = focusService.getFocusTodo(userDetails.getUserId());

		return ResponseEntity
				.status(result.successCode().getHttpStatus())
				.body(BaseResponse.onSuccess(result.successCode(), result.response()));
	}
}
