package com.Timo.Timo.domain.ai.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.ai.dto.request.RecommendDurationRequest;
import com.Timo.Timo.domain.ai.dto.response.GeminiDurationRecommendation;
import com.Timo.Timo.domain.ai.dto.response.RecommendDurationResponse;
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

	private static final int HISTORY_LIMIT = 5;
	private static final int ESTIMATED_RESPONSE_TOKEN_COST = 200;
	private static final int TOKEN_ESTIMATE_CHAR_DIVISOR = 4;

	private final AiTodoQueryRepository aiTodoQueryRepository;
	private final TodoDurationPromptBuilder promptBuilder;
	private final GeminiService geminiService;
	private final ObjectMapper objectMapper;
	private final AiRequestRateLimiter rateLimiter;

	public RecommendDurationResponse recommendDuration(Long userId, RecommendDurationRequest request) {
		LocalDate today = LocalDate.now(ZoneOffset.UTC);

		List<TodoDurationHistory> similarTitleHistories =
			aiTodoQueryRepository.findActualDurationHistoriesBySimilarTitle(
				userId,
				request.title(),
				today,
				HISTORY_LIMIT
			);
		List<TodoDurationHistory> recentTagHistories = request.tagId() == null
			? List.of()
			: aiTodoQueryRepository.findActualDurationHistoriesByTagId(
				userId,
				request.tagId(),
				today,
				HISTORY_LIMIT
			);

		String prompt = promptBuilder.build(
			request,
			similarTitleHistories,
			recentTagHistories
		);
		rateLimiter.validate(userId, estimateTokenCost(prompt));

		log.info(
			"AI duration recommendation histories loaded. similarTitle={}, recentTag={}",
			similarTitleHistories.size(),
			recentTagHistories.size()
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
		) {
			throw new IllegalArgumentException("Gemini recommendation has missing fields.");
		}

		int recommendedMinutes = normalizeMinutes(recommendation.recommendedMinutes());

		return new RecommendDurationResponse(recommendedMinutes);
	}

	private int normalizeMinutes(int minutes) {
		return Math.max(1, minutes);
	}

	private int estimateTokenCost(String prompt) {
		return Math.max(1, prompt.length() / TOKEN_ESTIMATE_CHAR_DIVISOR) + ESTIMATED_RESPONSE_TOKEN_COST;
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
