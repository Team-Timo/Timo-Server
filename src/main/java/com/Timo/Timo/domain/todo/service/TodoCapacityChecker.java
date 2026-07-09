package com.Timo.Timo.domain.todo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.exception.TodoErrorCode;
import com.Timo.Timo.domain.todo.repository.TodoRepository;
import com.Timo.Timo.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TodoCapacityChecker {

    private static final int MAX_TODO_COUNT_PER_DATE = 20;

    private final TodoRepository todoRepository;
    private final TodoDateCalculator todoDateCalculator;

    public void assertCapacity(Long userId, List<LocalDate> newRuleDates) {
        assertCapacity(userId, null, newRuleDates);
    }

    public void assertCapacity(Long userId, Long excludeTodoId, List<LocalDate> newRuleDates) {
        if (newRuleDates.isEmpty()) {
            return;
        }

        LocalDate from = newRuleDates.get(0);
        LocalDate to = newRuleDates.get(newRuleDates.size() - 1);

        List<Todo> existingRules = todoRepository.findRulesInRange(userId, from, to);

        for (LocalDate date : newRuleDates) {
            long existingCount = existingRules.stream()
                    .filter(rule -> !rule.getId().equals(excludeTodoId))
                    .filter(rule -> todoDateCalculator.occursOn(rule, date))
                    .count();

            if (existingCount >= MAX_TODO_COUNT_PER_DATE) {
                throw new CustomException(TodoErrorCode.MAX_COUNT_EXCEEDED);
            }
        }
    }
}
