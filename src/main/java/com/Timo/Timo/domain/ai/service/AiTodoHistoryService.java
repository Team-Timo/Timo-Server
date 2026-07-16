package com.Timo.Timo.domain.ai.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.ai.dto.TodoDurationHistory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiTodoHistoryService {

	private final AiHistoryAsyncQueryService aiHistoryAsyncQueryService;
	private final AiHistoryCacheService aiHistoryCacheService;

	@Transactional(readOnly = true)
	public AiTodoHistories findHistories(
		Long userId,
		String title,
		Long tagId,
		LocalDateTime toExclusive,
		ZoneId userZoneId,
		int limit
	) {
		AiHistoryCacheService.CacheLookupResult similarCacheResult =
			aiHistoryCacheService.getSimilarTitleHistories(
				userId,
				title,
				toExclusive,
				userZoneId,
				limit
			);
		CompletableFuture<List<TodoDurationHistory>> similarTitleFuture = similarCacheResult.hit()
			? CompletableFuture.completedFuture(similarCacheResult.histories())
			: aiHistoryAsyncQueryService.findSimilarTitleHistories(
				userId,
				title,
				toExclusive,
				userZoneId,
				limit
			).thenApply(histories -> {
				aiHistoryCacheService.cacheSimilarTitleHistories(
					userId,
					title,
					toExclusive,
					userZoneId,
					limit,
					histories
				);
				return histories;
				});

		CompletableFuture<List<TodoDurationHistory>> recentTagFuture;
		if (tagId == null) {
			recentTagFuture = CompletableFuture.completedFuture(List.of());
		} else {
			AiHistoryCacheService.CacheLookupResult tagCacheResult =
				aiHistoryCacheService.getRecentTagHistories(
					userId,
					tagId,
					toExclusive,
					userZoneId,
					limit
				);
			recentTagFuture = tagCacheResult.hit()
				? CompletableFuture.completedFuture(tagCacheResult.histories())
				: aiHistoryAsyncQueryService.findRecentTagHistories(
					userId,
					tagId,
					toExclusive,
					userZoneId,
					limit
				).thenApply(histories -> {
					aiHistoryCacheService.cacheRecentTagHistories(
						userId,
						tagId,
						toExclusive,
						userZoneId,
						limit,
						histories
					);
					return histories;
					});
		}

		List<TodoDurationHistory> similarTitleHistories = similarTitleFuture.join();
		List<TodoDurationHistory> recentTagHistories = recentTagFuture.join();

		return new AiTodoHistories(
			similarTitleHistories,
			recentTagHistories
		);
	}
}
