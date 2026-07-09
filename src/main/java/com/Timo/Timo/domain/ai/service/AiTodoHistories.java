package com.Timo.Timo.domain.ai.service;

import java.util.List;

import com.Timo.Timo.domain.ai.dto.TodoDurationHistory;

public record AiTodoHistories(
	List<TodoDurationHistory> similarTitleHistories,
	List<TodoDurationHistory> recentTagHistories
) {}