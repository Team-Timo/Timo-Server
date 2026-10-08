package com.Timo.Timo.domain.ai.prompt;

import java.util.List;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.ai.dto.TodoDurationHistory;

@Component
public class TodoHistoryPromptFormatter {

	public String formatHistories(List<TodoDurationHistory> histories) {
		if (histories == null || histories.isEmpty()) {
			return "요약: {\"count\":0}\n기록: []";
		}

		return "요약: %s\n기록: %s".formatted(summarize(histories), listHistories(histories));
	}

	public int toMinutes(Integer durationSeconds) {
		if (durationSeconds == null || durationSeconds <= 0) {
			return 0;
		}
		return Math.max(1, (int)Math.round(durationSeconds / 60.0));
	}

	public String escapeJsonString(String value) {
		if (value == null) {
			return "";
		}
		return value
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\n", " ")
			.replace("\r", " ")
			.replace("\t", " ");
	}

	private String summarize(List<TodoDurationHistory> histories) {
		List<Integer> minutes = histories.stream()
			.map(history -> toMinutes(history.actualSeconds()))
			.toList();
		int count = minutes.size();
		double avgSeconds = histories.stream()
			.mapToInt(TodoDurationHistory::actualSeconds)
			.average()
			.orElse(0);
		int avg = Math.max(1, (int)Math.round(avgSeconds / 60.0));
		int min = minutes.stream().mapToInt(Integer::intValue).min().orElse(0);
		int max = minutes.stream().mapToInt(Integer::intValue).max().orElse(0);

		return """
			{"count":%d,"avgMinutes":%d,"minMinutes":%d,"maxMinutes":%d}""".formatted(count, avg, min, max);
	}

	private String listHistories(List<TodoDurationHistory> histories) {
		return histories.stream()
			.map(history -> """
				{"title":"%s","date":"%s","actualMinutes":%d}
				""".formatted(
				escapeJsonString(history.title()),
				history.date(),
				toMinutes(history.actualSeconds())
			).trim())
			.toList()
			.toString();
	}
}
