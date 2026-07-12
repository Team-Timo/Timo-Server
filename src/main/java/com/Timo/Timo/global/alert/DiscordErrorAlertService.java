가package com.Timo.Timo.global.alert;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DiscordErrorAlertService {

	private static final int DISCORD_CONTENT_LIMIT = 2000;
	private static final int MESSAGE_LIMIT = 300;
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(2);

	private final RestClient restClient = createRestClient();

	@Value("${discord.alert.enabled:false}")
	private boolean enabled;

	@Value("${discord.webhook.url:}")
	private String webhookUrl;

	@Value("${spring.application.name:timo-server}")
	private String applicationName;

	@Value("${sentry.environment:${spring.profiles.active:local}}")
	private String environment;

	public void sendSentryWebhook(JsonNode payload) {
		if (!enabled || webhookUrl == null || webhookUrl.isBlank()) {
			return;
		}

		try {
			restClient.post()
				.uri(webhookUrl)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("content", buildContent(payload)))
				.retrieve()
				.toBodilessEntity();
		} catch (Exception alertException) {
			log.warn("Failed to send Discord error alert.", alertException);
		}
	}

	private String buildContent(JsonNode payload) {
		JsonNode issue = payload.path("data").path("issue");
		JsonNode event = payload.path("data").path("event");
		String action = text(payload, "action", "-");
		String resource = text(payload, "resource", "-");
		String title = firstText(
			issue.path("title").asText(null),
			event.path("title").asText(null),
			event.path("message").asText(null),
			"-"
		);
		String level = firstText(
			issue.path("level").asText(null),
			event.path("level").asText(null),
			"-"
		);
		String project = firstText(
			issue.path("project").path("slug").asText(null),
			issue.path("project").asText(null),
			event.path("project").asText(null),
			"-"
		);
		String url = firstText(
			issue.path("web_url").asText(null),
			issue.path("permalink").asText(null),
			event.path("web_url").asText(null),
			"-"
		);

		String content = """
			[ERROR] [%s] %s Sentry 알림
			```text
			action: %s
			resource: %s
			project: %s
			level: %s
			title: %s
			url: %s
			```
			""".formatted(
			environment,
			applicationName,
			action,
			resource,
			project,
			level,
			limit(title, MESSAGE_LIMIT),
			url
		).trim();

		return limit(content, DISCORD_CONTENT_LIMIT);
	}

	private String text(JsonNode node, String fieldName, String defaultValue) {
		String value = node.path(fieldName).asText(null);
		return value == null || value.isBlank() ? defaultValue : value;
	}

	private String firstText(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return "-";
	}

	private String limit(String value, int limit) {
		if (value == null || value.isBlank()) {
			return "-";
		}
		if (value.length() <= limit) {
			return value;
		}
		return value.substring(0, limit - 3) + "...";
	}

	private RestClient createRestClient() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);

		return RestClient.builder()
			.requestFactory(requestFactory)
			.build();
	}
}