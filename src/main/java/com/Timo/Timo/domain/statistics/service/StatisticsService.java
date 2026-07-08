package com.Timo.Timo.domain.statistics.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.statistics.dto.response.StatisticsCalendarResponse;
import com.Timo.Timo.domain.statistics.dto.response.StatisticsCalendarResponse.DayCompletionResponse;
import com.Timo.Timo.domain.statistics.dto.response.StatisticsDailyResponse;
import com.Timo.Timo.domain.statistics.dto.response.StatisticsDailyResponse.DailyTodoResponse;
import com.Timo.Timo.domain.statistics.dto.response.StatisticsDailyResponse.TagResponse;
import com.Timo.Timo.domain.statistics.dto.response.StatisticsSummaryResponse;
import com.Timo.Timo.domain.statistics.repository.StatisticsDailyTodo;
import com.Timo.Timo.domain.statistics.support.StatisticsDateParser;
import com.Timo.Timo.domain.timer.repository.TimerMonthlyRecordStats;
import com.Timo.Timo.domain.timer.repository.TimerRecordRepository;
import com.Timo.Timo.domain.todo.repository.TodoDailyCompletionStats;
import com.Timo.Timo.domain.todo.repository.TodoMonthlySummaryStats;
import com.Timo.Timo.domain.todo.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

	private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
	private static final int SECONDS_PER_MINUTE = 60;

	private final TodoRepository todoRepository;
	private final TimerRecordRepository timerRecordRepository;
	private final StatisticsDateParser statisticsDateParser;

	public StatisticsCalendarResponse getCalendar(Long userId, String yearMonthValue) {
		YearMonth yearMonth = statisticsDateParser.parseYearMonth(yearMonthValue);
		LocalDate today = LocalDate.now(ZoneOffset.UTC);
		LocalDate startDate = yearMonth.atDay(1);
		LocalDate endDate = yearMonth.atEndOfMonth();

		Map<LocalDate, TodoDailyCompletionStats> dailyStats = todoRepository.findDailyCompletionStats(
				userId,
				startDate,
				endDate
			).stream()
			.collect(Collectors.toMap(TodoDailyCompletionStats::getDate, Function.identity()));

		List<DayCompletionResponse> days = IntStream.rangeClosed(1, yearMonth.lengthOfMonth())
			.mapToObj(day -> {
				LocalDate date = yearMonth.atDay(day);
				return new DayCompletionResponse(date, calculateCompletionRate(dailyStats.get(date)));
			})
			.toList();

		return new StatisticsCalendarResponse(
			yearMonth.format(YEAR_MONTH_FORMATTER),
			today,
			days
		);
	}

	public StatisticsSummaryResponse getSummary(Long userId, String yearMonthValue) {
		YearMonth yearMonth = statisticsDateParser.parseYearMonth(yearMonthValue);
		LocalDate startDate = yearMonth.atDay(1);
		LocalDate nextMonthStartDate = yearMonth.plusMonths(1).atDay(1);

		TimerMonthlyRecordStats timerStats = timerRecordRepository.findMonthlyRecordStats(
			userId,
			startDate.atStartOfDay(),
			nextMonthStartDate.atStartOfDay()
		);
		TodoMonthlySummaryStats todoStats = todoRepository.findMonthlySummaryStats(
			userId,
			startDate.atStartOfDay(),
			nextMonthStartDate.atStartOfDay()
		);

		long totalRecordSeconds = timerStats.getTotalRecordSeconds();
		long timerRecordedDayCount = timerStats.getTimerRecordedDayCount();
		long averageRecordedMinutes = timerRecordedDayCount == 0
			? 0L
			: totalRecordSeconds / timerRecordedDayCount / SECONDS_PER_MINUTE;

		return new StatisticsSummaryResponse(
			totalRecordSeconds / SECONDS_PER_MINUTE,
			toInteger(todoStats.getActiveDayCount()),
			averageRecordedMinutes,
			toInteger(todoStats.getCompletedTodoCount()),
			toInteger(todoStats.getTotalTodoCount())
		);
	}

	public StatisticsDailyResponse getDaily(Long userId, String dateValue) {
		LocalDate date = statisticsDateParser.parseDate(dateValue);
		LocalDate nextDate = date.plusDays(1);
		long totalRecordSeconds = timerRecordRepository.sumDailyRecordSeconds(
			userId,
			date.atStartOfDay(),
			nextDate.atStartOfDay()
		);

		List<DailyTodoResponse> todos = todoRepository.findDailyTodos(
				userId,
				date,
				date.atStartOfDay(),
				nextDate.atStartOfDay()
			).stream()
			.map(this::toDailyTodoResponse)
			.toList();

		return new StatisticsDailyResponse(date, toMinutes(totalRecordSeconds), todos);
	}

	private int calculateCompletionRate(TodoDailyCompletionStats stats) {
		if (stats == null || stats.getTotalCount() == null || stats.getTotalCount() == 0) {
			return 0;
		}

		long completedCount = stats.getCompletedCount() == null ? 0 : stats.getCompletedCount();
		return (int)Math.round(completedCount * 100.0 / stats.getTotalCount());
	}

	private DailyTodoResponse toDailyTodoResponse(StatisticsDailyTodo todo) {
		return new DailyTodoResponse(
			todo.todoId(),
			todo.title(),
			toMinutes(todo.actualSeconds()),
			toMinutes(todo.estimatedSeconds()),
			toTagResponse(todo)
		);
	}

	private TagResponse toTagResponse(StatisticsDailyTodo todo) {
		if (todo.tagName() == null) {
			return null;
		}
		return new TagResponse(todo.tagName());
	}

	private long toMinutes(long seconds) {
		return seconds / SECONDS_PER_MINUTE;
	}

	private int toInteger(Long value) {
		if (value == null) {
			return 0;
		}
		return value.intValue();
	}
}