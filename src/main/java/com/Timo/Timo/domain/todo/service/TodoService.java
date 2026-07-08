package com.Timo.Timo.domain.todo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.tag.exception.TagErrorCode;
import com.Timo.Timo.domain.tag.repository.TagRepository;
import com.Timo.Timo.domain.todo.dto.request.TodoCreateRequest;
import com.Timo.Timo.domain.todo.dto.response.TodoCreateResponse;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.enums.RepeatType;
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
	private final UserRepository userRepository;
	private final TagRepository tagRepository;
	private final TodoDateCalculator todoDateCalculator;
	private final TodoCapacityChecker todoCapacityChecker;

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
