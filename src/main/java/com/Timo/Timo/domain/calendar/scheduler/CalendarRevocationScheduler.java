package com.Timo.Timo.domain.calendar.scheduler;

import com.Timo.Timo.domain.calendar.client.GoogleOAuthClient;
import com.Timo.Timo.domain.calendar.entity.CalendarRevocationOutbox;
import com.Timo.Timo.domain.calendar.enums.RevocationStatus;
import com.Timo.Timo.domain.calendar.repository.CalendarRevocationOutboxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarRevocationScheduler {

  private final CalendarRevocationOutboxRepository outboxRepository;
  private final GoogleOAuthClient googleOAuthClient;

  @Scheduled(fixedDelay = 60_000)
  @Transactional
  public void processPendingRevocations() {
    List<CalendarRevocationOutbox> pending = outboxRepository.findTop50ByStatus(RevocationStatus.PENDING);

    for (CalendarRevocationOutbox outbox : pending) {
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
