package com.Timo.Timo.domain.ai.prompt;

import java.util.List;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.ai.dto.request.RecommendDurationRequest;
import com.Timo.Timo.domain.ai.repository.TodoDurationHistory;

@Component
public class TodoDurationPromptBuilder {

	public String build(
		RecommendDurationRequest request,
		List<TodoDurationHistory> similarTitleHistories,
		List<TodoDurationHistory> recentTagHistories
	) {
		return """
			너는 Timo 투두 앱에서 사용자의 실제 작업 시간을 분석해 예상 소요 시간을 추천하는 시간 계획 코치야.
			사용자가 입력한 투두명을 먼저 이해하고, 과거 타이머 기록의 실제 소요시간 패턴을 참고해 현실적인 예상 소요 시간을 제안해.

			판단 방식:
			- 먼저 현재 투두명과 비슷한 과거 투두의 실제 소요시간을 확인해.
			- 그다음 사용자가 지정한 태그에서 최근 실제 소요시간 경향을 참고해.
			- 비슷한 투두명 기록과 태그 기록이 모두 있으면 둘을 함께 보고, 비슷한 투두명 기록을 조금 더 중요하게 봐.
			- 기록이 아예 없으면 현재 투두명만 기준으로 일반적인 예상 소요 시간을 판단해.

			규칙:
			- 응답은 반드시 JSON 객체 하나만 반환해.
			- recommendedMinutes는 분 단위 정수로 반환해.
			- 실제 기록에 없는 패턴은 만들지 마.

			응답 JSON 형식:
			{
			  "recommendedMinutes": 45
			}

			현재 투두:
			{
			  "title": "%s",
			  "tagId": %s
			}

			비슷한 투두명 실제 소요시간 기록:
			%s

			사용자가 지정한 태그의 최근 실제 소요시간 기록:
			%s
			""".formatted(
			escape(request.title()),
			request.tagId() == null ? "null" : request.tagId().toString(),
			formatHistories(similarTitleHistories),
			formatHistories(recentTagHistories)
		);
	}

	private String formatHistories(List<TodoDurationHistory> histories) {
		if (histories == null || histories.isEmpty()) {
			return "[]";
		}

		return histories.stream()
			.map(history -> """
				{"title":"%s","tagId":%s,"date":"%s","actualMinutes":%d}
				""".formatted(
				escape(history.title()),
				history.tagId() == null ? "null" : history.tagId().toString(),
				history.date(),
				toMinutes(history.actualSeconds())
			).trim())
			.toList()
			.toString();
	}

	private int toMinutes(Integer durationSeconds) {
		if (durationSeconds == null || durationSeconds <= 0) {
			return 0;
		}
		return Math.max(1, (int)Math.round(durationSeconds / 60.0));
	}

	private String escape(String value) {
		if (value == null) {
			return "";
		}
		return value
			.replace("\\", "\\\\")
			.replace("\"", "\\\"");
	}
}
