package com.Timo.Timo.domain.todo.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.tag.entity.Tag;
import com.Timo.Timo.domain.tag.exception.TagErrorCode;
import com.Timo.Timo.domain.tag.repository.TagRepository;
import com.Timo.Timo.domain.timer.service.TimerService;
import com.Timo.Timo.domain.todo.dto.request.TodoCreateRequest;
import com.Timo.Timo.domain.todo.dto.request.TodoReorderRequest;
import com.Timo.Timo.domain.todo.dto.request.TodoStatusUpdateRequest;
import com.Timo.Timo.domain.todo.dto.request.TodoSubtaskUpdateRequest;
import com.Timo.Timo.domain.todo.dto.request.TodoUpdateRequest;
import com.Timo.Timo.domain.todo.dto.response.TodoCreateResponse;
import com.Timo.Timo.domain.todo.dto.response.TodoDetailResponse;
import com.Timo.Timo.domain.todo.dto.response.TodoReorderResponse;
import com.Timo.Timo.domain.todo.dto.response.TodoStatusChangeResponse;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.entity.TodoInstance;
import com.Timo.Timo.domain.todo.enums.RepeatType;
import com.Timo.Timo.domain.todo.enums.Weekday;
import com.Timo.Timo.domain.todo.exception.TodoErrorCode;
import com.Timo.Timo.domain.todo.repository.TodoInstanceRepository;
import com.Timo.Timo.domain.todo.repository.TodoRepository;
import com.Timo.Timo.domain.todo.vo.Duration;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

	private final TodoRepository todoRepository;
	private final TodoInstanceRepository todoInstanceRepository;
	private final UserRepository userRepository;
	private final TagRepository tagRepository;
	private final TodoDateCalculator todoDateCalculator;
	private final TodoCapacityChecker todoCapacityChecker;
	private final TodoInstanceReorderer todoInstanceReorderer;
	private final TimerService timerService;

	@Transactional
	public TodoCreateResponse createTodo(Long userId, TodoCreateRequest request) {
		User user = userRepository.findByIdForUpdate(userId)
				.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		validateTagExists(request.tagId());

		Duration duration = Duration.parse(request.duration());
		List<LocalDate> todoDates = todoDateCalculator.calculate(request);
		todoCapacityChecker.assertCapacity(userId, todoDates);

		LocalDate startDate = request.date();
		LocalDate endDate = resolveEndDate(startDate, request.repeatType());

		Todo todo = Todo.create(
				user,
				request.icon(),
				request.title(),
				request.subtasks(),
				startDate,
				endDate,
				request.repeatType(),
				request.repeatWeekdays(),
				request.repeatDayOfMonth(),
				duration.seconds(),
				request.priority(),
				request.tagId(),
				request.memo()
		);

		Todo savedTodo = todoRepository.save(todo);
		return TodoCreateResponse.from(savedTodo);
	}

	public TodoDetailResponse getTodoDetail(Long userId, Long todoId, String dateValue) {
		Todo todo = todoRepository.findByIdAndUser_Id(todoId, userId)
				.orElseThrow(() -> new CustomException(TodoErrorCode.TODO_NOT_FOUND));

		LocalDate date = parseDate(dateValue);
		if (!todoDateCalculator.occursOn(todo, date)) {
			throw new CustomException(TodoErrorCode.TODO_NOT_FOUND_ON_DATE);
		}

		TodoInstance instance = todoInstanceRepository
				.findByTodo_IdAndDate(todo.getId(), date)
				.orElse(null);

		Tag tag = todo.getTagId() != null
				? tagRepository.findById(todo.getTagId()).orElse(null)
				: null;

		return TodoDetailResponse.of(todo, instance, date, tag);
	}

	@Transactional
	public TodoStatusChangeResponse changeCompletion(Long userId, Long todoId, TodoStatusUpdateRequest request) {
		if (request.isCompleted() == null) {
			throw new CustomException(TodoErrorCode.IS_COMPLETED_REQUIRED);
		}

		Todo todo = todoRepository.findByIdAndUser_Id(todoId, userId)
				.orElseThrow(() -> new CustomException(TodoErrorCode.TODO_NOT_FOUND));

		LocalDate date = resolveDate(userId, request.date());
		if (!todoDateCalculator.occursOn(todo, date)) {
			throw new CustomException(TodoErrorCode.TODO_NOT_FOUND);
		}

		if (timerService.hasActiveTimer(todoId)) {
			throw new CustomException(TodoErrorCode.TIMER_RUNNING);
		}

		TodoInstance instance = todoInstanceReorderer.applyCompletion(userId, todo, date, request.isCompleted());
		return TodoStatusChangeResponse.from(todoId, instance);
	}

	@Transactional
	public TodoReorderResponse reorderTodo(Long userId, Long todoId, TodoReorderRequest request) {
		Todo todo = todoRepository.findByIdAndUser_Id(todoId, userId)
				.orElseThrow(() -> new CustomException(TodoErrorCode.TODO_NOT_FOUND));

		LocalDate date = resolveDate(userId, request.date());
		if (!todoDateCalculator.occursOn(todo, date)) {
			throw new CustomException(TodoErrorCode.TODO_NOT_FOUND);
		}

		TodoInstance instance = todoInstanceReorderer.applyReorder(userId, todo, date, request.newIndex());
		return TodoReorderResponse.from(todoId, instance);
	}

	@Transactional
	public void updateTodo(Long userId, Long todoId, TodoUpdateRequest request) {
		if (request.hasNoUpdatableField()) {
			throw new CustomException(TodoErrorCode.NO_UPDATE_FIELDS);
		}

		Todo todo = todoRepository.findByIdAndUser_Id(todoId, userId)
				.orElseThrow(() -> new CustomException(TodoErrorCode.TODO_NOT_FOUND));

		// 타이머 실행 중에는 일정/소요시간 변경을 막는다. (그 외 필드 수정은 허용)
		boolean changesTimerSensitiveFields = request.durationSeconds() != null || isScheduleChanged(request);
		if (changesTimerSensitiveFields && timerService.hasActiveTimer(todoId)) {
			throw new CustomException(TodoErrorCode.TIMER_RUNNING);
		}

		validateTagExists(request.tagId());

		todo.updateFields(
				request.icon(),
				request.title(),
				request.durationSeconds(),
				request.priority(),
				request.tagId(),
				request.memo()
		);

		applyScheduleChange(todo, request);

		if (request.subtasks() != null) {
			todo.replaceSubtasks(toSubtaskEdits(request.subtasks()));
		}
	}

	@Transactional
	public void deleteTodo(Long userId, Long todoId) {
		Todo todo = todoRepository.findByIdAndUser_Id(todoId, userId)
				.orElseThrow(() -> new CustomException(TodoErrorCode.TODO_NOT_FOUND));

		if (timerService.hasActiveTimer(todoId)) {
			throw new CustomException(TodoErrorCode.TIMER_RUNNING);
		}

		timerService.deleteTimersByTodo(todoId);
		todoInstanceRepository.deleteByTodoId(todoId);
		todoRepository.delete(todo);
	}

	private boolean isScheduleChanged(TodoUpdateRequest request) {
		return request.date() != null
				|| request.repeatType() != null
				|| request.repeatWeekdays() != null
				|| request.repeatDayOfMonth() != null;
	}

	private void applyScheduleChange(Todo todo, TodoUpdateRequest request) {
		if (!isScheduleChanged(request)) {
			return;
		}

		LocalDate startDate = request.date() != null ? request.date() : todo.getStartDate();
		RepeatType repeatType = request.repeatType() != null ? request.repeatType() : todo.getRepeatType();
		List<Weekday> repeatWeekdays = request.repeatWeekdays() != null
				? request.repeatWeekdays() : todo.getRepeatWeekdays();
		Integer repeatDayOfMonth = request.repeatDayOfMonth() != null
				? request.repeatDayOfMonth() : todo.getRepeatDayOfMonth();

		validateRepeatRule(repeatType, repeatWeekdays, repeatDayOfMonth);

		Long userId = todo.getUser().getId();
		userRepository.findByIdForUpdate(userId)
				.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		List<LocalDate> todoDates = todoDateCalculator.calculate(startDate, repeatType, repeatWeekdays, repeatDayOfMonth);
		todoCapacityChecker.assertCapacity(userId, todo.getId(), todoDates);

		LocalDate endDate = resolveEndDate(startDate, repeatType);
		todo.changeSchedule(startDate, endDate, repeatType, repeatWeekdays, repeatDayOfMonth);
	}

	private void validateRepeatRule(RepeatType repeatType, List<Weekday> repeatWeekdays, Integer repeatDayOfMonth) {
		boolean valid = switch (repeatType) {
			case WEEKLY -> repeatWeekdays != null && !repeatWeekdays.isEmpty();
			case MONTHLY -> repeatDayOfMonth != null;
			case NONE, DAILY -> true;
		};
		if (!valid) {
			throw new CustomException(TodoErrorCode.INVALID_REQUEST);
		}
	}

	private LocalDate resolveDate(Long userId, LocalDate requestedDate) {
		if (requestedDate != null) {
			return requestedDate;
		}
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
		return LocalDate.now(ZoneId.of(user.getZoneId()));
	}

	private LocalDate parseDate(String dateValue) {
		if (dateValue == null || dateValue.isBlank()) {
			throw new CustomException(TodoErrorCode.INVALID_REQUEST);
		}

		try {
			return LocalDate.parse(dateValue);
		} catch (DateTimeParseException exception) {
			throw new CustomException(TodoErrorCode.INVALID_REQUEST);
		}
	}

	private List<Todo.SubtaskEdit> toSubtaskEdits(List<TodoSubtaskUpdateRequest> subtasks) {
		return subtasks.stream()
				.map(subtask -> new Todo.SubtaskEdit(
						subtask.subtaskId(),
						subtask.content(),
						subtask.completed()
				))
				.toList();
	}

	private void validateTagExists(Long tagId) {
		if (tagId != null && !tagRepository.existsById(tagId)) {
			throw new CustomException(TagErrorCode.TAG_NOT_FOUND);
		}
	}

	private LocalDate resolveEndDate(LocalDate startDate, RepeatType repeatType) {
		if (repeatType == RepeatType.NONE) {
			return startDate;
		}
		return startDate.plus(TodoDateCalculator.REPEAT_PERIOD);
	}
}
