package com.Timo.Timo.domain.ai.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.ai.dto.TodoDurationHistory;
import com.Timo.Timo.domain.ai.repository.AiTodoQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiTodoHistoryService {

	private final AiTodoQueryRepository aiTodoQueryRepository;

	@Transactional(readOnly = true)
	public AiTodoHistories findHistories(
		Long userId,
		String title,
		Long tagId,
		LocalDateTime toExclusive,
		ZoneId userZoneId,
		int limit
	) {
		List<TodoDurationHistory> similarTitleHistories =
			aiTodoQueryRepository.findActualDurationHistoriesBySimilarTitle(
				userId,
				title,
				toExclusive,
				userZoneId,
				limit
			);
		List<TodoDurationHistory> recentTagHistories = tagId == null
			? List.of()
			: aiTodoQueryRepository.findActualDurationHistoriesByTagId(
				userId,
				tagId,
				toExclusive,
				userZoneId,
				limit
			);

		return new AiTodoHistories(similarTitleHistories, recentTagHistories);
	}
}
