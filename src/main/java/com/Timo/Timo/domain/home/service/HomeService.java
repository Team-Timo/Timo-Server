package com.Timo.Timo.domain.home.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.home.dto.response.HomeResponse;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.DayResponse;
import com.Timo.Timo.domain.home.dto.response.HomeResponse.TodoResponse;
import com.Timo.Timo.domain.home.enums.HomeFilter;
import com.Timo.Timo.domain.home.exception.HomeErrorCode;
import com.Timo.Timo.domain.home.mapper.HomeTodoMapper;
import com.Timo.Timo.domain.home.mapper.HomeTodoMapper.TodoContext;
import com.Timo.Timo.domain.tag.entity.Tag;
import com.Timo.Timo.domain.tag.repository.TagRepository;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.entity.TodoInstance;
import com.Timo.Timo.domain.todo.repository.TodoInstanceRepository;
import com.Timo.Timo.domain.todo.repository.TodoRepository;
import com.Timo.Timo.domain.todo.service.TodoDateCalculator;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.enums.Language;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

	private final TodoRepository todoRepository;
	private final TodoInstanceRepository todoInstanceRepository;
	private final TagRepository tagRepository;
	private final TodoDateCalculator todoDateCalculator;
	private final HolidayChecker holidayChecker;
	private final HomeTodoMapper homeTodoMapper;
	private final UserRepository userRepository;

	public HomeResponse getHome(Long userId, String filterValue, String baseDateValue) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
		ZoneId userZone = ZoneId.of(user.getZoneId());
		Language language = user.getLanguage();
		HomeFilter filter = parseFilter(filterValue);
		LocalDate baseDate = parseBaseDate(baseDateValue, userZone);
		LocalDate from = resolveFrom(filter, baseDate);
		LocalDate to = resolveTo(filter, baseDate);
		List<LocalDate> dates = from.datesUntil(to.plusDays(1)).toList();

		List<Todo> rules = todoRepository.findRulesInRange(userId, from, to);
		List<Long> todoIds = rules.stream()
				.map(Todo::getId)
				.toList();

		Map<InstanceKey, TodoInstance> instancesByKey = todoIds.isEmpty()
				? Map.of()
				: todoInstanceRepository.findByTodoIdsAndDateRange(todoIds, from, to).stream()
						.collect(Collectors.toMap(
								instance -> new InstanceKey(instance.getTodo().getId(), instance.getDate()),
								Function.identity()
						));

		Map<Long, Tag> tagsById = loadTags(rules);
		LocalDate today = LocalDate.now(userZone);

		List<DayResponse> days = dates.stream()
				.map(date -> buildDay(date, today, language, rules, instancesByKey, tagsById))
				.toList();

		return new HomeResponse(filter, baseDate, days);
	}

	private DayResponse buildDay(
			LocalDate date,
			LocalDate today,
			Language language,
			List<Todo> rules,
			Map<InstanceKey, TodoInstance> instancesByKey,
			Map<Long, Tag> tagsById
	) {
		List<Todo> occurredRules = rules.stream()
				.filter(rule -> todoDateCalculator.occursOn(rule, date))
				.toList();

		List<TodoResponse> todos = new ArrayList<>();
		for (int index = 0; index < occurredRules.size(); index++) {
			Todo rule = occurredRules.get(index);
			TodoInstance instance = instancesByKey.get(new InstanceKey(rule.getId(), date));
			todos.add(homeTodoMapper.toResponse(
					new TodoContext(rule, instance, tagsById.get(rule.getTagId()), index)
			));
		}

		todos = todos.stream()
				.sorted(Comparator
						.comparing(TodoResponse::completed)
						.thenComparing(TodoResponse::sortOrder, Comparator.nullsLast(Integer::compareTo))
						.thenComparing(TodoResponse::todoId))
				.toList();

		return DayResponse.of(
				date,
				holidayChecker.isHoliday(date, language),
				date.equals(today),
				todos
		);
	}

	private Map<Long, Tag> loadTags(List<Todo> rules) {
		List<Long> tagIds = rules.stream()
				.map(Todo::getTagId)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

		if (tagIds.isEmpty()) {
			return Map.of();
		}

		return tagRepository.findAllById(tagIds).stream()
				.collect(Collectors.toMap(Tag::getId, Function.identity()));
	}

	private HomeFilter parseFilter(String filterValue) {
		if (filterValue == null || filterValue.isBlank()) {
			return HomeFilter.DEFAULT;
		}

		try {
			return HomeFilter.valueOf(filterValue);
		} catch (IllegalArgumentException exception) {
			throw new CustomException(HomeErrorCode.INVALID_FILTER_OR_DATE);
		}
	}

	private LocalDate parseBaseDate(String baseDateValue, ZoneId userZone) {
		if (baseDateValue == null || baseDateValue.isBlank()) {
			return LocalDate.now(userZone);
		}

		try {
			return LocalDate.parse(baseDateValue);
		} catch (DateTimeParseException exception) {
			throw new CustomException(HomeErrorCode.INVALID_DATE);
		}
	}

	private LocalDate resolveFrom(HomeFilter filter, LocalDate baseDate) {
		return switch (filter) {
			case DEFAULT -> baseDate.minusDays(7);
			case WEEK -> baseDate;
		};
	}

	private LocalDate resolveTo(HomeFilter filter, LocalDate baseDate) {
		return switch (filter) {
			case DEFAULT -> baseDate.plusDays(7);
			case WEEK -> baseDate.plusDays(6);
		};
	}

	private record InstanceKey(
			Long todoId,
			LocalDate date
	) {
	}
}
