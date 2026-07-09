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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorDto> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException exception,
		HttpServletRequest request
	) {
		return createErrorResponse(TagErrorCode.INVALID_TAG_ID, TagErrorCode.INVALID_TAG_ID.getMessage(), request);
	}

	private ResponseEntity<ErrorDto> createInvalidRequestResponse(String message, HttpServletRequest request) {
		return createErrorResponse(TagErrorCode.INVALID_REQUEST, message, request);
	}

	private ResponseEntity<ErrorDto> createErrorResponse(
		TagErrorCode errorCode,
		String message,
		HttpServletRequest request
	) {
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
