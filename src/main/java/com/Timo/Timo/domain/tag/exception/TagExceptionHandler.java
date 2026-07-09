package com.Timo.Timo.domain.tag.exception;

import java.time.LocalDateTime;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.Timo.Timo.domain.tag.controller.TagController;
import com.Timo.Timo.global.exception.dto.ErrorDto;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(assignableTypes = TagController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TagExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		FieldError fieldError = exception.getBindingResult().getFieldError();
		String message = fieldError != null && fieldError.getDefaultMessage() != null
			? fieldError.getDefaultMessage()
			: TagErrorCode.INVALID_REQUEST.getMessage();

		return createInvalidRequestResponse(message, request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorDto> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException exception,
		HttpServletRequest request
	) {
		return createInvalidRequestResponse(TagErrorCode.INVALID_REQUEST.getMessage(), request);
	}

	private ResponseEntity<ErrorDto> createInvalidRequestResponse(String message, HttpServletRequest request) {
		TagErrorCode errorCode = TagErrorCode.INVALID_REQUEST;
		ErrorDto response = new ErrorDto(
			LocalDateTime.now(),
			errorCode.getHttpStatus().value(),
			errorCode.getCode(),
			message,
			request.getRequestURI()
		);

		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(response);
	}
}
