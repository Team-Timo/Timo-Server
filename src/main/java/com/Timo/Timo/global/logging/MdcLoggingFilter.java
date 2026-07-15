package com.Timo.Timo.global.logging;

import java.io.IOException;
import java.util.regex.Pattern;
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

	private static final Pattern TRACE_ID_PATTERN = Pattern.compile("^[A-Za-z0-9-]{16,64}$");

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		String traceId = resolveTraceId(request);

		MDC.put("traceId", traceId);
		MDC.put("method", request.getMethod());
		MDC.put("uri", request.getRequestURI());
		response.setHeader(LoggingConstants.HEADER, traceId);

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
		String traceId = request.getHeader(LoggingConstants.HEADER);
		if (StringUtils.hasText(traceId) && TRACE_ID_PATTERN.matcher(traceId).matches()) {
			return traceId;
		}
		return UUID.randomUUID().toString().replace("-", "");
	}
}