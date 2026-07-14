package com.Timo.Timo.global.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoggingConstants {

	public static final String TRACE_ID = "traceId";
	public static final String HEADER = "X-Trace-Id";
	public static final String UNKNOWN = "-";
}