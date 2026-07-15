package com.Timo.Timo.global.exception;

import java.time.LocalDateTime;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.MDC;

import com.Timo.Timo.global.exception.code.BaseErrorCode;
import com.Timo.Timo.global.exception.code.ErrorCode;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.logging.LoggingConstants;

import io.sentry.Sentry;
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
		log.warn("Handled custom exception code={} path={}", exception.getErrorCode().getCode(), request.getRequestURI());
		return createErrorResponse(exception.getErrorCode(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		log.warn("Validation failed path={}", request.getRequestURI());
		return createErrorResponse(ErrorCode.BAD_REQUEST, request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorDto> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException exception,
		HttpServletRequest request
	) {
		log.warn("Unreadable request body path={}", request.getRequestURI());
		return createErrorResponse(ErrorCode.BAD_REQUEST, request);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorDto> handleHttpRequestMethodNotSupportedException(
		HttpRequestMethodNotSupportedException exception,
		HttpServletRequest request
	) {
		log.warn("Method not allowed path={}", request.getRequestURI());
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
		log.warn("Concurrency conflict path={}", request.getRequestURI());
		return createErrorResponse(ErrorCode.CONCURRENCY_CONFLICT, request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDto> handleException(
		Exception exception,
		HttpServletRequest request
	) {
		log.error("Unexpected exception occurred.", exception);
		Sentry.captureException(exception);
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
			request.getRequestURI(),
			resolveTraceId()
		);

		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(response);
	}

	private String resolveTraceId() {
		return MDC.get(LoggingConstants.TRACE_ID) != null
			? MDC.get(LoggingConstants.TRACE_ID)
			: LoggingConstants.UNKNOWN;
	}
}