package com.Timo.Timo.global.exception;

import java.time.LocalDateTime;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.Timo.Timo.global.exception.code.BaseErrorCode;
import com.Timo.Timo.global.exception.code.ErrorCode;
import com.Timo.Timo.global.exception.dto.ErrorDto;

import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorDto> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException exception,
		HttpServletRequest request
	) {
		return createErrorResponse(ErrorCode.BAD_REQUEST, request);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorDto> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException exception,
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

	@ExceptionHandler({
		PessimisticLockException.class,
		LockTimeoutException.class,
		PessimisticLockingFailureException.class
	})
	public ResponseEntity<ErrorDto> handlePessimisticLockException(
		RuntimeException exception,
		HttpServletRequest request
	) {
		return createErrorResponse(ErrorCode.CONCURRENCY_CONFLICT, request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDto> handleException(
		Exception exception,
		HttpServletRequest request
	) {
		log.error("Unexpected exception occurred.", exception);
		return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, request);
	}

	private ResponseEntity<ErrorDto> createErrorResponse(
		BaseErrorCode errorCode,
		HttpServletRequest request
	) {
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