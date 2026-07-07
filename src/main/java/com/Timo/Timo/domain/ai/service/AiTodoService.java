package com.Timo.Timo.domain.ai.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.ai.dto.request.RecommendDurationRequest;
import com.Timo.Timo.domain.ai.dto.response.GeminiDurationRecommendation;
import com.Timo.Timo.domain.ai.dto.response.RecommendDurationResponse;
import com.Timo.Timo.domain.ai.enums.PatternBasis;
import com.Timo.Timo.domain.ai.prompt.TodoDurationPromptBuilder;
import com.Timo.Timo.domain.ai.repository.AiTodoQueryRepository;
import com.Timo.Timo.domain.ai.repository.TodoDurationHistory;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiTodoService {

	private static final int HISTORY_LIMIT = 10;

	private final AiTodoQueryRepository aiTodoQueryRepository;
	private final TodoDurationPromptBuilder promptBuilder;
	private final GeminiService geminiService;
	private final ObjectMapper objectMapper;

	@Value("${ai.duration.min-minutes:5}")
	private int minMinutes;

	@Value("${ai.duration.max-minutes:240}")
	private int maxMinutes;

	@Value("${ai.duration.default-minutes:30}")
	private int defaultMinutes;

	public RecommendDurationResponse recommendDuration(Long userId, RecommendDurationRequest request) {
		LocalDate today = LocalDate.now(ZoneOffset.UTC);

		List<TodoDurationHistory> sameTagSimilarTitleHistories = request.tagId() == null
			? List.of()
			: aiTodoQueryRepository.findActualDurationHistoriesBySimilarTitleAndTag(
				userId,
				request.title(),
				request.tagId(),
				today,
				HISTORY_LIMIT
			);
		List<TodoDurationHistory> sameTagHistories = request.tagId() == null
			? List.of()
			: aiTodoQueryRepository.findActualDurationHistoriesByTagId(
				userId,
				request.tagId(),
				today,
				HISTORY_LIMIT
			);
		List<TodoDurationHistory> recentHistories = aiTodoQueryRepository.findRecentActualDurationHistories(
			userId,
			today,
			HISTORY_LIMIT
		);

		String prompt = promptBuilder.build(
			request,
			sameTagSimilarTitleHistories,
			sameTagHistories,
			recentHistories,
			minMinutes,
			maxMinutes
		);

		log.info(
			"AI duration recommendation histories loaded. sameTagSimilarTitle={}, sameTag={}, recent={}",
			sameTagSimilarTitleHistories.size(),
			sameTagHistories.size(),
			recentHistories.size()
		);

		String geminiJson = geminiService.generateJson(prompt);
		GeminiDurationRecommendation recommendation = parseRecommendation(geminiJson);
		return validate(recommendation);
	}

	private GeminiDurationRecommendation parseRecommendation(String geminiJson) {
		try {
			return objectMapper.readValue(
				stripMarkdownFence(geminiJson),
				GeminiDurationRecommendation.class
			);
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to parse Gemini duration recommendation.", exception);
		}
	}

	private RecommendDurationResponse validate(GeminiDurationRecommendation recommendation) {
		if (recommendation == null
			|| recommendation.recommendedMinutes() == null
			|| recommendation.patternBasis() == null
			|| recommendation.feedback() == null
			|| recommendation.feedback().isBlank()
		) {
			throw new IllegalArgumentException("Gemini recommendation has missing fields.");
		}

		int recommendedMinutes = normalizeMinutes(recommendation.recommendedMinutes());

		return new RecommendDurationResponse(
			recommendedMinutes,
			recommendation.patternBasis(),
			recommendation.feedback()
		);
	}

	private int normalizeMinutes(int minutes) {
		int rounded = Math.round(minutes / 5.0f) * 5;
		return Math.max(minMinutes, Math.min(maxMinutes, rounded));
	}

	private String stripMarkdownFence(String value) {
		String trimmed = value.trim();
		if (trimmed.startsWith("```json")) {
			return trimmed.substring(7, trimmed.length() - 3).trim();
		}
		if (trimmed.startsWith("```")) {
			return trimmed.substring(3, trimmed.length() - 3).trim();
		}
		return trimmed;
	}
}
