package com.Timo.Timo.domain.calendar.google.scheduler;

import com.Timo.Timo.domain.calendar.google.client.GoogleOAuthClient;
import com.Timo.Timo.domain.calendar.google.entity.GoogleTokenRevocationOutbox;
import com.Timo.Timo.domain.calendar.google.enums.RevocationStatus;
import com.Timo.Timo.domain.calendar.google.repository.GoogleTokenRevocationOutboxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleTokenRevocationScheduler {

  private final GoogleTokenRevocationOutboxRepository outboxRepository;
  private final GoogleOAuthClient googleOAuthClient;

  @Scheduled(fixedDelay = 60_000)
  @Transactional
  public void processPendingRevocations() {
    List<GoogleTokenRevocationOutbox> pending = outboxRepository.findTop50ByStatus(RevocationStatus.PENDING);

    for (GoogleTokenRevocationOutbox outbox : pending) {
      try {
        googleOAuthClient.revokeToken(outbox.getToken());
        outbox.markCompleted();
      } catch (Exception e) {
        log.warn("구글 토큰 revoke 재시도 실패. outboxId={}, retryCount={}", outbox.getId(), outbox.getRetryCount(), e);
        outbox.markFailed();
      }
    }
  }
}
