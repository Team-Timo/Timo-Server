package com.Timo.Timo.global.analytics;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AnalyticsEventSender {

  private final MixpanelAPI mixpanelApi = new MixpanelAPI();
  private final MessageBuilder messageBuilder;
  private final Executor analyticsExecutor;

  public AnalyticsEventSender(
      @Value("${mixpanel.token:}") String token,
      @Qualifier("analyticsExecutor") Executor analyticsExecutor
  ) {
    this.messageBuilder = token == null || token.isBlank() ? null : new MessageBuilder(token);
    this.analyticsExecutor = analyticsExecutor;
  }

  public void send(Long userId, AnalyticsEvent event) {
    send(userId, event, Map.of());
  }

  public void send(Long userId, AnalyticsEvent event, Map<String, ?> properties) {
    if (messageBuilder == null || userId == null) {
      return;
    }

    JSONObject message;
    try {
      message = messageBuilder.event(String.valueOf(userId), event.getEventName(), new JSONObject(properties));
    } catch (RuntimeException exception) {
      log.warn("Failed to build analytics event. event={}", event.getEventName(), exception);
      return;
    }

    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          dispatch(event, message);
        }
      });
      return;
    }

    dispatch(event, message);
  }

  private void dispatch(AnalyticsEvent event, JSONObject message) {
    try {
      analyticsExecutor.execute(() -> deliver(event, message));
    } catch (RejectedExecutionException exception) {
      log.warn("Analytics event dropped because executor is full. event={}", event.getEventName());
    }
  }

  private void deliver(AnalyticsEvent event, JSONObject message) {
    try {
      mixpanelApi.sendMessage(message);
    } catch (Exception exception) {
      log.warn("Failed to send analytics event. event={}", event.getEventName(), exception);
    }
  }
}
