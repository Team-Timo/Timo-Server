package com.Timo.Timo.global.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.Timo.Timo.global.exception.CustomException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingAspect {

	@Around("execution(* com.Timo.Timo..service..*(..))")
	public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.currentTimeMillis();
		String signature = joinPoint.getSignature().toShortString();

		try {
			Object result = joinPoint.proceed();
			long durationMs = System.currentTimeMillis() - startTime;
			log.info("Service completed target={} durationMs={}", signature, durationMs);
			return result;
		} catch (CustomException exception) {
			long durationMs = System.currentTimeMillis() - startTime;
			log.warn(
				"Service handled exception target={} durationMs={} errorCode={}",
				signature,
				durationMs,
				exception.getErrorCode().getCode()
			);
			throw exception;
		} catch (Exception exception) {
			long durationMs = System.currentTimeMillis() - startTime;
			log.error(
				"Service exception target={} durationMs={} exceptionType={}",
				signature,
				durationMs,
				exception.getClass().getSimpleName(),
				exception
			);
			throw exception;
		}
	}
}