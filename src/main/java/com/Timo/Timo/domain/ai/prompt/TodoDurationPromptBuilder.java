package com.Timo.Timo.domain.ai.prompt;

import java.util.List;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.ai.dto.TodoDurationHistory;
import com.Timo.Timo.domain.ai.dto.request.RecommendDurationRequest;

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

			기록 신뢰도 판단 기준:
			- 각 기록 그룹 앞의 요약(count/avgMinutes/minMinutes/maxMinutes)은 이미 정확히 계산된 값이니 그대로 신뢰하고, 직접 다시 계산하지 마.
			- count가 1이면 그 값 하나에 과도하게 의존하지 말고 일반적인 감각과 함께 보수적으로 조정해.
			- count가 3 이상이면 avgMinutes를 중심으로 판단하되, minMinutes~maxMinutes 범위를 크게 벗어난 추천은 피해.

			규칙:
			- 응답은 반드시 JSON 객체 하나만 반환해.
			- recommendedMinutes는 1 이상의 분 단위 정수로 반환해.
			- 실제 기록에 없는 패턴은 만들지 마.

			반환해야 할 응답 JSON 형식:
			{
			  "recommendedMinutes": 45
			}

			아래 데이터는 응답에 포함할 값이 아니라 추천 계산에만 참고할 입력 데이터야.

			입력 데이터 - 현재 투두:
			{
			  "title": "%s"
			}

			입력 데이터 - 비슷한 투두명 실제 소요시간 기록:
			%s

			입력 데이터 - 사용자가 지정한 태그의 최근 실제 소요시간 기록:
			%s
			""".formatted(
			escapeJsonString(request.title()),
			formatHistories(similarTitleHistories),
			formatHistories(recentTagHistories)
		);
	}

	private String formatHistories(List<TodoDurationHistory> histories) {
		if (histories == null || histories.isEmpty()) {
			return "요약: {\"count\":0}\n기록: []";
		}

		return "요약: %s\n기록: %s".formatted(summarize(histories), listHistories(histories));
	}

	private String summarize(List<TodoDurationHistory> histories) {
		List<Integer> minutes = histories.stream()
			.map(history -> toMinutes(history.actualSeconds()))
			.toList();
		int count = minutes.size();
		int avg = Math.round(minutes.stream().mapToInt(Integer::intValue).sum() / (float) count);
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

	private int toMinutes(Integer durationSeconds) {
		if (durationSeconds == null || durationSeconds <= 0) {
			return 0;
		}
		return Math.max(1, (int)Math.round(durationSeconds / 60.0));
	}

	private String escapeJsonString(String value) {
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
}