package com.Timo.Timo.global.logging;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

public class MdcTaskDecorator implements TaskDecorator {

	@Override
	public Runnable decorate(Runnable runnable) {
		Map<String, String> callerContext = MDC.getCopyOfContextMap();
		return () -> {
			Map<String, String> previousContext = MDC.getCopyOfContextMap();
			setContext(callerContext);
			try {
				runnable.run();
			} finally {
				setContext(previousContext);
			}
		};
	}

	private void setContext(Map<String, String> context) {
		if (context == null) {
			MDC.clear();
		} else {
			MDC.setContextMap(context);
		}
	}
}
