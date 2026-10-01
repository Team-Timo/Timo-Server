package com.Timo.Timo.domain.ai.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.ai.dto.TodoDurationHistory;
import com.Timo.Timo.domain.ai.repository.AiTodoQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiHistoryAsyncQueryService {

  private final AiTodoQueryRepository aiTodoQueryRepository;

  @Async("aiHistoryExecutor")
  @Transactional(readOnly = true)
  public CompletableFuture<List<TodoDurationHistory>> findSimilarTitleHistories(
      Long userId,
      String title,
      LocalDateTime toExclusive,
      ZoneId userZoneId,
      int limit
  ) {
    return CompletableFuture.completedFuture(
        aiTodoQueryRepository.findActualDurationHistoriesBySimilarTitle(
            userId,
            title,
            toExclusive,
            userZoneId,
            limit
        )
    );
  }

  @Async("aiHistoryExecutor")
  @Transactional(readOnly = true)
  public CompletableFuture<List<TodoDurationHistory>> findRecentTagHistories(
      Long userId,
      Long tagId,
      LocalDateTime toExclusive,
      ZoneId userZoneId,
      int limit
  ) {
    return CompletableFuture.completedFuture(
        aiTodoQueryRepository.findActualDurationHistoriesByTagId(
            userId,
            tagId,
            toExclusive,
            userZoneId,
            limit
        )
    );
  }
}