package com.Timo.Timo.domain.ai.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.ai.repository.AiTodoQueryRepository;
import com.Timo.Timo.domain.ai.repository.TodoDurationHistory;

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
		LocalDate today,
		int limit
	) {
		List<TodoDurationHistory> similarTitleHistories =
			aiTodoQueryRepository.findActualDurationHistoriesBySimilarTitle(
				userId,
				title,
				today,
				limit
			);
		List<TodoDurationHistory> recentTagHistories = tagId == null
			? List.of()
			: aiTodoQueryRepository.findActualDurationHistoriesByTagId(
				userId,
				tagId,
				today,
				limit
			);

		return new AiTodoHistories(similarTitleHistories, recentTagHistories);
	}
}
