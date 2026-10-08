package com.Timo.Timo.domain.ai.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.timer.repository.TimerRecordRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiFeedbackPersistenceService {

  private final TimerRecordRepository timerRecordRepository;

  @Async("aiHistoryExecutor")
  @Transactional
  public void persistFeedback(Long timerId, String feedback) {
    try {
      timerRecordRepository.findById(timerId)
          .ifPresentOrElse(
              timerRecord -> timerRecord.updateAiFeedback(feedback),
              () -> log.warn("AI 피드백 저장 대상 타이머 기록을 찾을 수 없습니다. timerId={}", timerId)
          );
    } catch (Exception exception) {
      log.error("AI 피드백 저장 실패. timerId={}", timerId, exception);
    }
  }
}