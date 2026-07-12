package com.Timo.Timo.global.alert;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sentry")
public class SentryWebhookController {

	private final DiscordErrorAlertService discordErrorAlertService;

	@Value("${sentry.webhook.secret:}")
	private String webhookSecret;

	@PostMapping("/webhook")
	public ResponseEntity<Void> receive(
		@RequestParam(required = false) String token,
		@RequestHeader(value = "X-Sentry-Webhook-Secret", required = false) String secretHeader,
		@org.springframework.web.bind.annotation.RequestBody JsonNode payload
	) {
		if (!isAuthorized(token, secretHeader)) {
			log.warn("Unauthorized Sentry webhook request rejected.");
			return ResponseEntity.status(401).build();
		}

		discordErrorAlertService.sendSentryWebhook(payload);
		return ResponseEntity.ok().build();
	}

	private boolean isAuthorized(String token, String secretHeader) {
		if (webhookSecret == null || webhookSecret.isBlank()) {
			return true;
		}
		return webhookSecret.equals(token) || webhookSecret.equals(secretHeader);
	}
}정