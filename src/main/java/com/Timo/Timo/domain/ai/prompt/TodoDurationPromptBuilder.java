package com.Timo.Timo.domain.ai.prompt;

import java.util.List;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.ai.dto.request.RecommendDurationRequest;
import com.Timo.Timo.domain.ai.repository.TodoDurationHistory;

@Component
public class TodoDurationPromptBuilder {

	public String build(
		RecommendDurationRequest request,
		List<TodoDurationHistory> sameTagSimilarTitleHistories,
		List<TodoDurationHistory> sameTagHistories,
		List<TodoDurationHistory> recentHistories,
		int minMinutes,
		int maxMinutes
	) {
		return """
			너는 Timo 투두 앱의 예상 소요 시간 추천 AI야.
			현재 투두명/태그와 사용자의 타이머 기반 실제 소요시간 기록을 보고 예상 소요 시간을 추천해.

			판단 방식:
			- 같은 태그 안에서 투두명이 비슷한 기록을 가장 중요하게 참고해.
			- 같은 태그의 전체 기록을 함께 참고해서 평균/경향을 보정해.
			- 위 기록이 부족하면 사용자의 최근 타이머 기록 경향을 참고해.
			- 모든 기록이 비어 있으면 현재 투두명과 태그만 기준으로 판단해.

			규칙:
			- 응답은 반드시 JSON 객체 하나만 반환해.
			- recommendedMinutes는 5분 단위 정수여야 해.
			- recommendedMinutes는 %d분 이상 %d분 이하만 가능해.
			- feedback은 한국어 1문장으로 부드럽게 작성해.
			- 실제 기록에 없는 패턴은 만들지 마.
			- patternBasis는 가장 크게 참고한 근거를 기준으로 SIMILAR_TITLE, SAME_TAG, RECENT_HISTORY, CURRENT_ONLY 중 하나만 사용해.

			응답 JSON 형식:
			{
			  "recommendedMinutes": 45,
			  "patternBasis": "SAME_TAG",
			  "feedback": "같은 태그의 실제 소요시간 기록을 기준으로 45분 정도를 추천해요."
			}

			현재 투두:
			{
			  "title": "%s",
			  "tagId": %s
			}

			같은 태그 내 비슷한 투두명 기록:
			%s

			같은 태그 기록:
			%s

			최근 기록:
			%s
			""".formatted(
			minMinutes,
			maxMinutes,
			escape(request.title()),
			request.tagId() == null ? "null" : request.tagId().toString(),
			formatHistories(sameTagSimilarTitleHistories),
			formatHistories(sameTagHistories),
			formatHistories(recentHistories)
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
