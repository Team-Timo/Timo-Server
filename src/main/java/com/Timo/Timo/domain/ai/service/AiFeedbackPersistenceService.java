package com.Timo.Timo.domain.ai.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.timer.repository.TimerRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiFeedbackPersistenceService {

  private final TimerRecordRepository timerRecordRepository;

  @Async("aiHistoryExecutor")
  @Transactional
  public void persistFeedback(Long timerId, String feedback) {
    timerRecordRepository.findById(timerId)
        .ifPresent(timerRecord -> timerRecord.updateAiFeedback(feedback));
  }
}