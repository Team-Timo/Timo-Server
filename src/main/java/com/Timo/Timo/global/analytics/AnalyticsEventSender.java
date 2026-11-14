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
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

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
  private static final String DELETION_FAILED_METRIC = "analytics.deletions.failed";
  private static final String INSERT_ID_PROPERTY = "$insert_id";
  private static final int QUEUE_CAPACITY = 500;
  private static final int MAX_BATCH_SIZE = 50;
  private static final int TOO_MANY_REQUESTS = 429;
  private static final long POLL_TIMEOUT_MILLIS = 1_000;
  private static final long SHUTDOWN_TIMEOUT_MILLIS = 5_000;
  private static final long DELETION_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(1);

  private final MixpanelAPI mixpanelApi = new MixpanelAPI();
  private final BlockingQueue<PendingTask> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
  private final MessageBuilder messageBuilder;
  private final MixpanelDataDeletionClient dataDeletionClient;
  private final MeterRegistry meterRegistry;

  private volatile boolean running;
  private Thread worker;
  private long lastDeletionRequestedAt;

  public AnalyticsEventSender(
      @Value("${mixpanel.token:}") String token,
      MixpanelDataDeletionClient dataDeletionClient,
      MeterRegistry meterRegistry
  ) {
    this.messageBuilder = token == null || token.isBlank() ? null : new MessageBuilder(token);
    this.dataDeletionClient = dataDeletionClient;
    this.meterRegistry = meterRegistry;
  }

  @PostConstruct
  void start() {
    if (messageBuilder == null) {
      return;
    }
    if (!dataDeletionClient.isConfigured()) {
      log.warn("Mixpanel GDPR token is not configured. User data deletion on withdrawal is disabled.");
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

    List<PendingTask> remaining = new ArrayList<>();
    queue.drainTo(remaining);
    if (!remaining.isEmpty()) {
      log.warn("Analytics tasks dropped on shutdown. size={}", remaining.size());
      remaining.forEach(task -> recordDropped(task, DropReason.SHUTDOWN_TIMEOUT));
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
    sendAnonymous(event, Map.of());
  }

  public void sendAnonymous(AnalyticsEvent event, Map<String, ?> properties) {
    publish("", event, properties);
  }

  public void requestUserDataDeletion(Long userId) {
    if (userId == null || messageBuilder == null) {
      return;
    }

    PendingDeletion pendingDeletion = new PendingDeletion(String.valueOf(userId));
    runAfterCommit(() -> {
      if (!dataDeletionClient.isConfigured()) {
        log.warn("Analytics user data deletion skipped because GDPR token is not configured. userId={}", userId);
        recordDeletionFailed(DropReason.NOT_CONFIGURED);
        return;
      }
      enqueue(pendingDeletion);
    });
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
    runAfterCommit(() -> enqueue(pendingEvent));
  }

  private void runAfterCommit(Runnable action) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          action.run();
        }
      });
      return;
    }

    action.run();
  }

  private void enqueue(PendingTask task) {
    if (queue.offer(task)) {
      return;
    }
    if (task instanceof PendingDeletion deletion) {
      log.warn("Analytics user data deletion dropped because queue is full. userId={}", deletion.distinctId());
    } else if (task instanceof PendingEvent pendingEvent) {
      log.warn("Analytics event dropped because queue is full. event={}", pendingEvent.event().getEventName());
    }
    recordDropped(task, DropReason.QUEUE_FULL);
  }

  private void processQueue() {
    while (running || !queue.isEmpty()) {
      try {
        PendingTask first = queue.poll(POLL_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        if (first == null) {
          continue;
        }
        List<PendingTask> batch = new ArrayList<>(MAX_BATCH_SIZE);
        batch.add(first);
        queue.drainTo(batch, MAX_BATCH_SIZE - 1);
        process(batch);
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        return;
      } catch (RuntimeException exception) {
        log.warn("Unexpected error while sending analytics events.", exception);
      }
    }
  }

  private void process(List<PendingTask> batch) throws InterruptedException {
    List<PendingEvent> events = new ArrayList<>();
    for (PendingTask task : batch) {
      if (task instanceof PendingEvent pendingEvent) {
        events.add(pendingEvent);
        continue;
      }
      if (!events.isEmpty()) {
        deliver(events);
        events = new ArrayList<>();
      }
      delete((PendingDeletion) task);
    }
    if (!events.isEmpty()) {
      deliver(events);
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

  private void delete(PendingDeletion deletion) throws InterruptedException {
    try {
      requestDeletion(deletion);
    } catch (RestClientException exception) {
      if (!isRetryable(exception)) {
        recordDeletionFailed(deletion, exception);
        return;
      }
      retryDeletion(deletion);
    } catch (RuntimeException exception) {
      recordDeletionFailed(deletion, exception);
    }
  }

  private void retryDeletion(PendingDeletion deletion) throws InterruptedException {
    try {
      requestDeletion(deletion);
    } catch (RuntimeException exception) {
      recordDeletionFailed(deletion, exception);
    }
  }

  private void requestDeletion(PendingDeletion deletion) throws InterruptedException {
    waitForDeletionInterval();
    try {
      dataDeletionClient.requestDeletion(deletion.distinctId());
    } finally {
      lastDeletionRequestedAt = System.nanoTime();
    }
  }

  private void waitForDeletionInterval() throws InterruptedException {
    if (lastDeletionRequestedAt == 0) {
      return;
    }
    long remainingNanos = DELETION_INTERVAL_NANOS - (System.nanoTime() - lastDeletionRequestedAt);
    if (remainingNanos > 0) {
      TimeUnit.NANOSECONDS.sleep(remainingNanos);
    }
  }

  private boolean isRetryable(RestClientException exception) {
    if (exception instanceof HttpClientErrorException clientError) {
      return clientError.getStatusCode().value() == TOO_MANY_REQUESTS;
    }
    return true;
  }

  private void recordDeletionFailed(PendingDeletion deletion, RuntimeException exception) {
    String status = exception instanceof HttpStatusCodeException statusError
        ? String.valueOf(statusError.getStatusCode().value())
        : "none";
    log.warn("Failed to request analytics user data deletion. userId={}, status={}, cause={}",
        deletion.distinctId(), status,
        NestedExceptionUtils.getMostSpecificCause(exception).getClass().getSimpleName());
    recordDeletionFailed(DropReason.SEND_FAILED);
  }

  private void recordDropped(List<PendingEvent> batch) {
    batch.forEach(pendingEvent -> recordDropped(pendingEvent.event(), DropReason.SEND_FAILED));
  }

  private void recordDropped(PendingTask task, DropReason reason) {
    if (task instanceof PendingEvent pendingEvent) {
      recordDropped(pendingEvent.event(), reason);
      return;
    }
    recordDeletionFailed(reason);
  }

  private void recordDropped(AnalyticsEvent event, DropReason reason) {
    Counter.builder(DROPPED_METRIC)
        .tag("event", event.getEventName())
        .tag("reason", reason.getTag())
        .register(meterRegistry)
        .increment();
  }

  private void recordDeletionFailed(DropReason reason) {
    Counter.builder(DELETION_FAILED_METRIC)
        .tag("reason", reason.getTag())
        .register(meterRegistry)
        .increment();
  }

  private sealed interface PendingTask permits PendingEvent, PendingDeletion {
  }

  private record PendingEvent(AnalyticsEvent event, JSONObject message) implements PendingTask {
  }

  private record PendingDeletion(String distinctId) implements PendingTask {
  }

  private enum DropReason {
    BUILD_FAILED("build_failed"),
    QUEUE_FULL("queue_full"),
    SEND_FAILED("send_failed"),
    SHUTDOWN_TIMEOUT("shutdown_timeout"),
    NOT_CONFIGURED("not_configured");

    private final String tag;

    DropReason(String tag) {
      this.tag = tag;
    }

    String getTag() {
      return tag;
    }
  }
}
