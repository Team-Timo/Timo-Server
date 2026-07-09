package com.Timo.Timo.domain.ai.service;

import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

import com.Timo.Timo.domain.ai.dto.request.RecommendDurationRequest;
import com.Timo.Timo.domain.ai.dto.response.GeminiDurationRecommendation;
import com.Timo.Timo.domain.ai.dto.response.RecommendDurationResponse;
import com.Timo.Timo.domain.ai.prompt.TodoDurationPromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTodoService {

	private static final int HISTORY_LIMIT = 5;
	private static final int ESTIMATED_RESPONSE_TOKEN_COST = 200;
	private static final int TOKEN_ESTIMATE_CHAR_DIVISOR = 4;

	private final AiTodoHistoryService historyService;
	private final TodoDurationPromptBuilder promptBuilder;
	private final GeminiService geminiService;
	private final ObjectMapper objectMapper;
	private final AiRequestRateLimiter rateLimiter;

	public RecommendDurationResponse recommendDuration(Long userId, RecommendDurationRequest request) {
		LocalDate today = LocalDate.now(ZoneOffset.UTC);

		AiTodoHistories histories = historyService.findHistories(
			userId,
			request.title(),
			request.tagId(),
			today,
			HISTORY_LIMIT
		);

		String prompt = promptBuilder.build(
			request,
			histories.similarTitleHistories(),
			histories.recentTagHistories()
		);
		rateLimiter.validate(userId, estimateTokenCost(prompt));

		log.info(
			"AI duration recommendation histories loaded. similarTitle={}, recentTag={}",
			histories.similarTitleHistories().size(),
			histories.recentTagHistories().size()
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
		if (trimmed.startsWith("```json") && trimmed.endsWith("```")) {
			return trimmed.substring(7, trimmed.length() - 3).trim();
		}
		if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
			return trimmed.substring(3, trimmed.length() - 3).trim();
		}
		return trimmed;
	}
}
