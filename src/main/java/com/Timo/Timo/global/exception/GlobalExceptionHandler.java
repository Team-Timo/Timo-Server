package com.Timo.Timo.global.exception;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.Timo.Timo.global.exception.code.BaseErrorCode;
import com.Timo.Timo.global.exception.code.ErrorCode;
import com.Timo.Timo.global.exception.dto.ErrorDto;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorDto> handleCustomException(
		CustomException exception,
		HttpServletRequest request
	) {
		return createErrorResponse(exception.getErrorCode(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		return createErrorResponse(ErrorCode.BAD_REQUEST, request);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorDto> handleHttpRequestMethodNotSupportedException(
		HttpRequestMethodNotSupportedException exception,
		HttpServletRequest request
	) {
		return createErrorResponse(ErrorCode.METHOD_NOT_ALLOWED, request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDto> handleException(
		Exception exception,
		HttpServletRequest request
	) {
		return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, request);
	}

	private ResponseEntity<ErrorDto> createErrorResponse(
		BaseErrorCode errorCode,
		HttpServletRequest request
	) {
		ErrorDto response = new ErrorDto(
			LocalDateTime.now().toString(),
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
