package com.Timo.Timo.domain.focus.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.focus.dto.FocusTodoResult;
import com.Timo.Timo.domain.focus.dto.response.FocusTodoResponse;
import com.Timo.Timo.domain.focus.exception.FocusSuccessCode;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.TodoResponse;
import com.Timo.Timo.domain.home.service.HomeTodoReader;
import com.Timo.Timo.domain.home.service.HomeTodoReader.LoadedTodos;
import com.Timo.Timo.domain.timer.entity.TimerRecord;
import com.Timo.Timo.domain.timer.enums.TimerStatus;
import com.Timo.Timo.domain.timer.repository.TimerRecordRepository;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FocusService {

	private static final List<TimerStatus> ACTIVE_TIMER_STATUSES = List.of(TimerStatus.RUNNING, TimerStatus.PAUSED);

	private final UserRepository userRepository;
	private final HomeTodoReader homeTodoReader;
	private final TimerRecordRepository timerRecordRepository;

	public FocusTodoResult getFocusTodo(Long userId) {
		User user = getUser(userId);
		LocalDate today = LocalDate.now(ZoneId.of(user.getZoneId()));

		FocusTodoResult activeTimerFocus = resolveActiveTimerFocus(userId);
		if (activeTimerFocus != null) {
			return activeTimerFocus;
		}

		LoadedTodos loaded = homeTodoReader.load(userId, today, today);
		List<TodoResponse> todos = homeTodoReader.sortedTodosOn(loaded, today);

		if (todos.isEmpty()) {
			return new FocusTodoResult(FocusSuccessCode.NO_TODO_TODAY, FocusTodoResponse.empty(today));
		}

		TodoResponse focusTodo = todos.stream()
				.filter(todo -> !todo.completed())
				.findFirst()
				.orElse(null);

		if (focusTodo == null) {
			return new FocusTodoResult(FocusSuccessCode.ALL_TODO_COMPLETED, FocusTodoResponse.empty(today));
		}

		String memo = loaded.rules().stream()
				.filter(rule -> rule.getId().equals(focusTodo.todoId()))
				.findFirst()
				.map(Todo::getMemo)
				.orElse(null);

		return new FocusTodoResult(FocusSuccessCode.GET_FOCUS_TODO, FocusTodoResponse.of(today, focusTodo, memo));
	}

	private FocusTodoResult resolveActiveTimerFocus(Long userId) {
		TimerRecord activeTimer = timerRecordRepository
				.findByUserIdAndStatusIn(userId, ACTIVE_TIMER_STATUSES)
				.orElse(null);
		if (activeTimer == null) {
			return null;
		}

		Todo todo = activeTimer.getTodo();
		LocalDate timerDate = activeTimer.getStartedAt().toLocalDate();

		LoadedTodos loaded = homeTodoReader.load(userId, timerDate, timerDate);
		TodoResponse focusTodo = homeTodoReader.sortedTodosOn(loaded, timerDate).stream()
				.filter(response -> response.todoId().equals(todo.getId()))
				.findFirst()
				.orElse(null);
		if (focusTodo == null) {
			return null;
		}

		return new FocusTodoResult(
				FocusSuccessCode.GET_FOCUS_TODO,
				FocusTodoResponse.of(timerDate, focusTodo, todo.getMemo())
		);
	}

	private User getUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
	}
}
