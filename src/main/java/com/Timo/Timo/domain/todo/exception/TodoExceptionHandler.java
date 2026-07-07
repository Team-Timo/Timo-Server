package com.Timo.Timo.domain.todo.exception;

import java.time.LocalDateTime;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.Timo.Timo.domain.todo.controller.TodoController;
import com.Timo.Timo.global.exception.dto.ErrorDto;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(assignableTypes = TodoController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TodoExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		return createInvalidRequestResponse(request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorDto> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException exception,
		HttpServletRequest request
	) {
		return createInvalidRequestResponse(request);
	}

	private ResponseEntity<ErrorDto> createInvalidRequestResponse(HttpServletRequest request) {
		TodoErrorCode errorCode = TodoErrorCode.INVALID_REQUEST;
		ErrorDto response = new ErrorDto(
			LocalDateTime.now(),
			errorCode.getHttpStatus().value(),
			errorCode.getCode(),
			errorCode.getMessage(),
			request.getRequestURI()
		);

		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(response);
	}
}
