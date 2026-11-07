package com.Timo.Timo.domain.statistics.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import com.Timo.Timo.domain.statistics.support.StatisticsDateParser;
import com.Timo.Timo.domain.statistics.support.StatisticsOccurrenceCalculator;
import com.Timo.Timo.domain.statistics.support.StatisticsOccurrenceCalculator.DailyOccurrence;
import com.Timo.Timo.domain.tag.entity.Tag;
import com.Timo.Timo.domain.tag.repository.TagRepository;
import com.Timo.Timo.domain.timer.repository.TimerRecordRepository;
import com.Timo.Timo.domain.timer.repository.TimerStatisticsRecord;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.analytics.AnalyticsEvent;
import com.Timo.Timo.global.analytics.AnalyticsEventSender;
import com.Timo.Timo.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

	private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
	private static final int SECONDS_PER_MINUTE = 60;

	private final TimerRecordRepository timerRecordRepository;
	private final TagRepository tagRepository;
	private final StatisticsDateParser statisticsDateParser;
	private final StatisticsOccurrenceCalculator statisticsOccurrenceCalculator;
	private final UserRepository userRepository;
	private final AnalyticsEventSender analyticsEventSender;

	public StatisticsCalendarResponse getCalendar(Long userId, String yearMonthValue) {
		ZoneId userZone = getUserZone(userId);
		analyticsEventSender.send(userId, AnalyticsEvent.STATISTICS_VIEWED, Map.of("type", "calendar"));
		YearMonth yearMonth = statisticsDateParser.parseYearMonth(yearMonthValue);
		LocalDate today = LocalDate.now(userZone);
		LocalDate startDate = yearMonth.atDay(1);
		LocalDate endDate = yearMonth.atEndOfMonth();

		Map<LocalDate, DailyOccurrence> dailyStats = statisticsOccurrenceCalculator.calculateDailyOccurrences(
			userId,
			startDate,
			endDate
		);

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
		ZoneId userZone = getUserZone(userId);
		analyticsEventSender.send(userId, AnalyticsEvent.STATISTICS_VIEWED, Map.of("type", "summary"));
		YearMonth yearMonth = statisticsDateParser.parseYearMonth(yearMonthValue);
		LocalDate startDate = yearMonth.atDay(1);
		LocalDate endDate = yearMonth.atEndOfMonth();

		List<ResolvedTimerRecord> timerRecords = findTimerRecords(userId, startDate, endDate, userZone);
		Map<LocalDate, DailyOccurrence> occurrencesByDate = statisticsOccurrenceCalculator
			.calculateDailyOccurrences(userId, startDate, endDate);
		Collection<DailyOccurrence> dailyOccurrences = occurrencesByDate.values();

		Map<LocalDate, Long> recordMinutesByDate = sumCompletedTodoMinutesByDate(timerRecords, occurrencesByDate);
		long totalRecordMinutes = recordMinutesByDate.values().stream().mapToLong(Long::longValue).sum();
		long recordedDayCount = recordMinutesByDate.values().stream().filter(minutes -> minutes > 0).count();

		Set<LocalDate> activeDates = timerRecords.stream()
			.map(ResolvedTimerRecord::date)
			.collect(Collectors.toCollection(HashSet::new));
		dailyOccurrences.stream()
			.filter(day -> day.completedCount() > 0)
			.map(DailyOccurrence::date)
			.forEach(activeDates::add);
		int activeDayCount = activeDates.size();
		int totalTodoCount = dailyOccurrences.stream().mapToInt(DailyOccurrence::totalCount).sum();
		int completedTodoCount = dailyOccurrences.stream().mapToInt(DailyOccurrence::completedCount).sum();
		long averageRecordedMinutes = recordedDayCount == 0
			? 0L
			: totalRecordMinutes / recordedDayCount;

		return new StatisticsSummaryResponse(
			totalRecordMinutes,
			activeDayCount,
			averageRecordedMinutes,
			completedTodoCount,
			totalTodoCount
		);
	}

	public StatisticsDailyResponse getDaily(Long userId, String dateValue) {
		ZoneId userZone = getUserZone(userId);
		analyticsEventSender.send(userId, AnalyticsEvent.STATISTICS_VIEWED, Map.of("type", "daily"));
		LocalDate date = statisticsDateParser.parseDate(dateValue);

		List<ResolvedTimerRecord> timerRecords = findTimerRecords(userId, date, date, userZone);
		Map<Long, Long> actualSecondsByTodoId = timerRecords.stream()
			.collect(Collectors.groupingBy(
				ResolvedTimerRecord::todoId,
				Collectors.summingLong(ResolvedTimerRecord::actualSeconds)
			));

		List<Todo> dailyTodos = statisticsOccurrenceCalculator.findDailyCompletedTodos(userId, date);
		Map<Long, String> tagNamesById = findTagNames(dailyTodos);
		List<DailyTodoResponse> todos = dailyTodos.stream()
			.map(todo -> toDailyTodoResponse(todo, actualSecondsByTodoId, tagNamesById))
			.toList();
		long totalRecordMinutes = todos.stream().mapToLong(DailyTodoResponse::actualTimeMinutes).sum();

		return new StatisticsDailyResponse(date, totalRecordMinutes, todos);
	}

	private ZoneId getUserZone(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
		return ZoneId.of(user.getZoneId());
	}

	private LocalDateTime toUtcStartOfDay(LocalDate date, ZoneId userZone) {
		return date.atStartOfDay(userZone)
			.withZoneSameInstant(ZoneOffset.UTC)
			.toLocalDateTime();
	}

	private List<ResolvedTimerRecord> findTimerRecords(Long userId, LocalDate from, LocalDate to, ZoneId userZone) {
		LocalDateTime fromInclusive = toUtcStartOfDay(from, userZone);
		LocalDateTime toExclusive = toUtcStartOfDay(to.plusDays(1), userZone);

		return timerRecordRepository.findStatisticsRecords(userId, from, to, fromInclusive, toExclusive).stream()
			.map(timerRecord -> new ResolvedTimerRecord(
				timerRecord.getTodoId(),
				resolveRecordDate(timerRecord, userZone),
				timerRecord.getActualSeconds()
			))
			.filter(timerRecord -> !timerRecord.date().isBefore(from) && !timerRecord.date().isAfter(to))
			.toList();
	}

	private LocalDate resolveRecordDate(TimerStatisticsRecord timerRecord, ZoneId userZone) {
		if (timerRecord.getTargetDate() != null) {
			return timerRecord.getTargetDate();
		}
		return timerRecord.getStartedAt()
			.atZone(ZoneOffset.UTC)
			.withZoneSameInstant(userZone)
			.toLocalDate();
	}

	private Map<LocalDate, Long> sumCompletedTodoMinutesByDate(
		List<ResolvedTimerRecord> timerRecords,
		Map<LocalDate, DailyOccurrence> occurrencesByDate
	) {
		Map<LocalDate, Map<Long, Long>> secondsByDateAndTodoId = timerRecords.stream()
			.filter(timerRecord -> isCompletedOn(occurrencesByDate.get(timerRecord.date()), timerRecord.todoId()))
			.collect(Collectors.groupingBy(
				ResolvedTimerRecord::date,
				Collectors.groupingBy(
					ResolvedTimerRecord::todoId,
					Collectors.summingLong(ResolvedTimerRecord::actualSeconds)
				)
			));

		return secondsByDateAndTodoId.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().values().stream().mapToLong(this::toRoundedMinutes).sum()
			));
	}

	private boolean isCompletedOn(DailyOccurrence occurrence, Long todoId) {
		return occurrence != null && occurrence.completedTodoIds().contains(todoId);
	}

	private int calculateCompletionRate(DailyOccurrence occurrence) {
		if (occurrence == null || occurrence.totalCount() == 0) {
			return 0;
		}

		return (int)Math.round(occurrence.completedCount() * 100.0 / occurrence.totalCount());
	}

	private DailyTodoResponse toDailyTodoResponse(
		Todo todo,
		Map<Long, Long> actualSecondsByTodoId,
		Map<Long, String> tagNamesById
	) {
		return new DailyTodoResponse(
			todo.getId(),
			todo.getTitle(),
			toRoundedMinutes(actualSecondsByTodoId.getOrDefault(todo.getId(), 0L)),
			toMinutes(todo.getDurationSeconds()),
			toTagResponse(todo.getTagId(), tagNamesById)
		);
	}

	private Map<Long, String> findTagNames(List<Todo> todos) {
		List<Long> tagIds = todos.stream()
			.map(Todo::getTagId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();

		return tagRepository.findAllById(tagIds).stream()
			.collect(Collectors.toMap(Tag::getId, Tag::getName));
	}

	private TagResponse toTagResponse(Long tagId, Map<Long, String> tagNamesById) {
		if (tagId == null || !tagNamesById.containsKey(tagId)) {
			return null;
		}
		return new TagResponse(tagNamesById.get(tagId));
	}

	private long toMinutes(long seconds) {
		return seconds / SECONDS_PER_MINUTE;
	}

	private long toRoundedMinutes(long seconds) {
		return Math.round((double)seconds / SECONDS_PER_MINUTE);
	}

	private record ResolvedTimerRecord(Long todoId, LocalDate date, long actualSeconds) {
	}
}
