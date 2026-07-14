package com.Timo.Timo.global.logging;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MdcLoggingFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		String traceId = resolveTraceId(request);

		MDC.put(LoggingConstants.TRACE_ID, traceId);
		MDC.put(LoggingConstants.HTTP_METHOD, request.getMethod());
		MDC.put(LoggingConstants.REQUEST_URI, request.getRequestURI());
		response.setHeader(LoggingConstants.TRACE_ID_HEADER, traceId);

		log.info("Request started");

		try {
			filterChain.doFilter(request, response);
		} finally {
			long durationMs = System.currentTimeMillis() - startTime;
			log.info("Request completed status={} durationMs={}", response.getStatus(), durationMs);
			MDC.clear();
		}
	}

	private String resolveTraceId(HttpServletRequest request) {
		String traceId = request.getHeader(LoggingConstants.TRACE_ID_HEADER);
		if (StringUtils.hasText(traceId)) {
			return traceId;
		}
		return UUID.randomUUID().toString().replace("-", "");
	}
}