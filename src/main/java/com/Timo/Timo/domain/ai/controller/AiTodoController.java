package com.Timo.Timo.domain.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Timo.Timo.domain.ai.docs.AiFeedbackDocs;
import com.Timo.Timo.domain.ai.docs.AiTodoDocs;
import com.Timo.Timo.domain.ai.dto.request.CreateTodoFeedbackRequest;
import com.Timo.Timo.domain.ai.dto.request.RecommendDurationRequest;
import com.Timo.Timo.domain.ai.dto.response.CreateTodoFeedbackResponse;
import com.Timo.Timo.domain.ai.dto.response.RecommendDurationResponse;
import com.Timo.Timo.domain.ai.exception.AiSuccessCode;
import com.Timo.Timo.domain.ai.service.AiTodoService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "AI Todo", description = "투두 AI API")
public class AiTodoController implements AiTodoDocs, AiFeedbackDocs {

	private final AiTodoService aiTodoService;

	@Override
	@PostMapping("/todos/recommend-duration")
	public ResponseEntity<BaseResponse<RecommendDurationResponse>> recommendDuration(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody RecommendDurationRequest request
	) {
		log.info(
			"AI duration recommendation API called. userId={}, tagId={}",
			userDetails.getUserId(),
			request.tagId()
		);

		RecommendDurationResponse response = aiTodoService.recommendDuration(
			userDetails.getUserId(),
			request
		);

		return ResponseEntity.ok(
			BaseResponse.onSuccess(AiSuccessCode.DURATION_RECOMMENDED, response)
		);
	}

	@Override
	@PostMapping("/ai/feedback")
	public ResponseEntity<BaseResponse<CreateTodoFeedbackResponse>> createFeedback(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody CreateTodoFeedbackRequest request
	) {
		log.info(
			"AI todo feedback API called. userId={}, todoId={}",
			userDetails.getUserId(),
			request.todoId()
		);

		CreateTodoFeedbackResponse response = aiTodoService.createFeedback(
			userDetails.getUserId(),
			request
		);

		return ResponseEntity.ok(
			BaseResponse.onSuccess(AiSuccessCode.TODO_FEEDBACK_CREATED, response)
		);
	}
}