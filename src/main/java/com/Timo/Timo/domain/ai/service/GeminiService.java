package com.Timo.Timo.domain.ai.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeminiService {

	private static final String GENERATE_CONTENT_URL =
		"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

	private final ObjectMapper objectMapper;
	private final RestClient restClient = createRestClient();

	@Value("${ai.gemini.api-key:}")
	private String apiKey;

	@Value("${ai.gemini.model}")
	private String model;

	public String generateJson(String prompt) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException("Gemini API key is not configured.");
		}
		if (model == null || model.isBlank()) {
			throw new IllegalStateException("Gemini model is not configured.");
		}

		Map<String, Object> request = Map.of(
			"contents", List.of(Map.of(
				"parts", List.of(Map.of("text", prompt))
			)),
			"generationConfig", Map.of(
				"temperature", 0.2,
				"response_mime_type", "application/json"
			)
		);

		String response = restClient.post()
			.uri(GENERATE_CONTENT_URL, model)
			.contentType(MediaType.APPLICATION_JSON)
			.header("x-goog-api-key", apiKey)
			.body(request)
			.retrieve()
			.body(String.class);

		return extractText(response);
	}

	private RestClient createRestClient() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);

		return RestClient.builder()
			.requestFactory(requestFactory)
			.build();
	}

	private String extractText(String response) {
		try {
			JsonNode root = objectMapper.readTree(response);
			JsonNode textNode = root
				.path("candidates")
				.path(0)
				.path("content")
				.path("parts")
				.path(0)
				.path("text");

			if (textNode.isMissingNode() || textNode.asText().isBlank()) {
				throw new IllegalStateException("Gemini response text is empty.");
			}

			return textNode.asText();
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to parse Gemini response.", exception);
		}
	}
}