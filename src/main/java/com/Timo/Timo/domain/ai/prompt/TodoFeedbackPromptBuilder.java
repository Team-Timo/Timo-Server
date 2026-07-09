package com.Timo.Timo.domain.ai.prompt;

import java.util.List;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.ai.dto.TodoDurationHistory;
import com.Timo.Timo.domain.ai.dto.TodoFeedbackSource;

@Component
public class TodoFeedbackPromptBuilder {

	public String build(
		TodoFeedbackSource source,
		List<TodoDurationHistory> similarTitleHistories,
		List<TodoDurationHistory> recentTagHistories
	) {
		return """
			너는 Timo 투두 앱에서 사용자의 실제 작업 시간을 분석해 짧은 피드백을 작성하는 시간 계획 코치야.
			이번 태스크의 예상 소요 시간과 실제 소요 시간을 비교하고, 과거 기록을 참고해 다음 계획을 제안해.

			피드백 판단 구조:
			1. 현재 결과 관찰
			- 이번 태스크가 예상 시간보다 길어졌는지, 거의 맞았는지, 일찍 끝났는지 짧게 분석해.
			2. 패턴 해석
			- 패턴 근거는 아래 우선순위로 선택해.
			- 1순위: 비슷한 투두명 실제 소요시간 기록
			- 2순위: 같은 태그의 최근 실제 소요시간 기록
			- 3순위: 기록이 없으면 이번 태스크의 연장 또는 조기 종료 여부
			3. 다음 행동 추천
			- 다음에 예상 시간을 어떻게 잡으면 좋을지 제안해.

			규칙:
			- 응답은 반드시 JSON 객체 하나만 반환해.
			- feedback은 한국어 1~2문장으로 자연스럽게 작성해.
			- feedback은 현재 결과 관찰, 패턴 해석, 다음 행동 추천을 압축해서 포함해.
			- 실제 기록에 없는 패턴은 만들지 마.
			- 기록이 부족하면 부족하다고 길게 말하지 말고, 이번 결과 기준으로만 제안해.
			- 다음 예상 시간은 분 단위로 제안해.

			반환해야 할 응답 JSON 형식:
			{
			  "feedback": "이번 작업은 예상보다 조금 길어졌어요. 비슷한 Work 태그의 작업들도 살짝 길어지는 편이라, 다음에는 60분 정도로 잡아보면 좋아요."
			}

			아래 데이터는 응답에 포함할 값이 아니라 피드백 생성에만 참고할 입력 데이터야.

			입력 데이터 - 이번 태스크:
			{
			  "title": "%s",
			  "tagName": "%s",
			  "estimatedMinutes": %d,
			  "actualMinutes": %d
			}

			입력 데이터 - 비슷한 투두명 실제 소요시간 기록:
			%s

			입력 데이터 - 같은 태그의 최근 실제 소요시간 기록:
			%s
			""".formatted(
			escapeJsonString(source.title()),
			escapeJsonString(source.tagName()),
			toMinutes(source.estimatedSeconds()),
			toMinutes(source.actualSeconds()),
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