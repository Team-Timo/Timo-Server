package com.Timo.Timo.global.analytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import com.mixpanel.mixpanelapi.MixpanelServerException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AnalyticsEventSender {

  private static final String DROPPED_METRIC = "analytics.events.dropped";
  private static final String INSERT_ID_PROPERTY = "$insert_id";
  private static final int QUEUE_CAPACITY = 500;
  private static final int MAX_BATCH_SIZE = 50;
  private static final long POLL_TIMEOUT_MILLIS = 1_000;
  private static final long SHUTDOWN_TIMEOUT_MILLIS = 5_000;

  private final MixpanelAPI mixpanelApi = new MixpanelAPI();
  private final BlockingQueue<PendingEvent> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
  private final MessageBuilder messageBuilder;
  private final MeterRegistry meterRegistry;

  private volatile boolean running;
  private Thread worker;

  public AnalyticsEventSender(
      @Value("${mixpanel.token:}") String token,
      MeterRegistry meterRegistry
  ) {
    this.messageBuilder = token == null || token.isBlank() ? null : new MessageBuilder(token);
    this.meterRegistry = meterRegistry;
  }

  @PostConstruct
  void start() {
    if (messageBuilder == null) {
      return;
    }
    running = true;
    worker = new Thread(this::processQueue, "analytics-sender");
    worker.setDaemon(true);
    worker.start();
  }

  @PreDestroy
  void stop() throws InterruptedException {
    if (worker == null) {
      return;
    }
    running = false;
    worker.join(SHUTDOWN_TIMEOUT_MILLIS);

    List<PendingEvent> remaining = new ArrayList<>();
    queue.drainTo(remaining);
    if (!remaining.isEmpty()) {
      log.warn("Analytics events dropped on shutdown. size={}", remaining.size());
      remaining.forEach(pendingEvent -> recordDropped(pendingEvent.event(), DropReason.SHUTDOWN_TIMEOUT));
    }
  }

  public void send(Long userId, AnalyticsEvent event) {
    send(userId, event, Map.of());
  }

  public void send(Long userId, AnalyticsEvent event, Map<String, ?> properties) {
    if (userId == null) {
      return;
    }
    publish(String.valueOf(userId), event, properties);
  }

  public void sendAnonymous(AnalyticsEvent event) {
    publish("", event, Map.of());
  }

  private void publish(String distinctId, AnalyticsEvent event, Map<String, ?> properties) {
    if (messageBuilder == null) {
      return;
    }

    JSONObject message;
    try {
      JSONObject eventProperties = new JSONObject(properties);
      eventProperties.put(INSERT_ID_PROPERTY, UUID.randomUUID().toString());
      message = messageBuilder.event(distinctId, event.getEventName(), eventProperties);
    } catch (RuntimeException exception) {
      log.warn("Failed to build analytics event. event={}", event.getEventName(), exception);
      recordDropped(event, DropReason.BUILD_FAILED);
      return;
    }

    PendingEvent pendingEvent = new PendingEvent(event, message);
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          enqueue(pendingEvent);
        }
      });
      return;
    }

    enqueue(pendingEvent);
  }

  private void enqueue(PendingEvent pendingEvent) {
    if (!queue.offer(pendingEvent)) {
      log.warn("Analytics event dropped because queue is full. event={}", pendingEvent.event().getEventName());
      recordDropped(pendingEvent.event(), DropReason.QUEUE_FULL);
    }
  }

  private void processQueue() {
    while (running || !queue.isEmpty()) {
      try {
        PendingEvent first = queue.poll(POLL_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        if (first == null) {
          continue;
        }
        List<PendingEvent> batch = new ArrayList<>(MAX_BATCH_SIZE);
        batch.add(first);
        queue.drainTo(batch, MAX_BATCH_SIZE - 1);
        deliver(batch);
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        return;
      } catch (RuntimeException exception) {
        log.warn("Unexpected error while sending analytics events.", exception);
      }
    }
  }

  private void deliver(List<PendingEvent> batch) {
    try {
      ClientDelivery delivery = new ClientDelivery();
      batch.forEach(pendingEvent -> delivery.addMessage(pendingEvent.message()));
      send(delivery, batch);
    } catch (RuntimeException exception) {
      log.warn("Unexpected error while sending analytics events. size={}", batch.size(), exception);
      recordDropped(batch);
    }
  }

  private void send(ClientDelivery delivery, List<PendingEvent> batch) {
    try {
      mixpanelApi.deliver(delivery);
    } catch (MixpanelServerException exception) {
      log.warn("Mixpanel rejected analytics events. size={}", batch.size(), exception);
      recordDropped(batch);
    } catch (IOException exception) {
      retry(delivery, batch);
    }
  }

  private void retry(ClientDelivery delivery, List<PendingEvent> batch) {
    try {
      mixpanelApi.deliver(delivery);
    } catch (IOException exception) {
      log.warn("Failed to send analytics events. size={}", batch.size(), exception);
      recordDropped(batch);
    }
  }

  private void recordDropped(List<PendingEvent> batch) {
    batch.forEach(pendingEvent -> recordDropped(pendingEvent.event(), DropReason.SEND_FAILED));
  }

  private void recordDropped(AnalyticsEvent event, DropReason reason) {
    Counter.builder(DROPPED_METRIC)
        .tag("event", event.getEventName())
        .tag("reason", reason.getTag())
        .register(meterRegistry)
        .increment();
  }

  private record PendingEvent(AnalyticsEvent event, JSONObject message) {
  }

  private enum DropReason {
    BUILD_FAILED("build_failed"),
    QUEUE_FULL("queue_full"),
    SEND_FAILED("send_failed"),
    SHUTDOWN_TIMEOUT("shutdown_timeout");

    private final String tag;

    DropReason(String tag) {
      this.tag = tag;
    }

    String getTag() {
      return tag;
    }
  }
}
