package com.Timo.Timo.domain.todo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Timo.Timo.domain.todo.docs.TodoControllerDocs;
import com.Timo.Timo.domain.todo.dto.request.TodoCreateRequest;
import com.Timo.Timo.domain.todo.dto.response.TodoCreateResponse;
import com.Timo.Timo.domain.todo.exception.TodoSuccessCode;
import com.Timo.Timo.domain.todo.service.TodoService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
@Tag(name = "Todo", description = "TODO API")
public class TodoController implements TodoControllerDocs {

	private final TodoService todoService;

	@Override
	@PostMapping
	public ResponseEntity<BaseResponse<TodoCreateResponse>> createTodo(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody TodoCreateRequest request
	) {
		TodoCreateResponse response = todoService.createTodo(userDetails.getUserId(), request);

		return ResponseEntity
			.status(TodoSuccessCode.CREATED.getHttpStatus())
			.body(BaseResponse.onSuccess(TodoSuccessCode.CREATED, response));
	}
}
