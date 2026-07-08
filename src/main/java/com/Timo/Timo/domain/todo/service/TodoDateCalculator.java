package com.Timo.Timo.domain.todo.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.todo.dto.request.TodoCreateRequest;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.enums.RepeatType;
import com.Timo.Timo.domain.todo.enums.Weekday;
import com.Timo.Timo.domain.todo.exception.TodoErrorCode;
import com.Timo.Timo.global.exception.CustomException;

@Component
public class TodoDateCalculator {

    public static final Period REPEAT_PERIOD = Period.ofYears(1);

    private static final Map<Weekday, DayOfWeek> DAY_OF_WEEKS = Map.of(
            Weekday.MON, DayOfWeek.MONDAY,
            Weekday.TUE, DayOfWeek.TUESDAY,
            Weekday.WED, DayOfWeek.WEDNESDAY,
            Weekday.THU, DayOfWeek.THURSDAY,
            Weekday.FRI, DayOfWeek.FRIDAY,
            Weekday.SAT, DayOfWeek.SATURDAY,
            Weekday.SUN, DayOfWeek.SUNDAY
    );

    public List<LocalDate> calculate(TodoCreateRequest request) {
        return calculate(
                request.date(),
                request.repeatType(),
                request.repeatWeekdays(),
                request.repeatDayOfMonth()
        );
    }

    public List<LocalDate> calculate(
            LocalDate start,
            RepeatType repeatType,
            List<Weekday> repeatWeekdays,
            Integer repeatDayOfMonth
    ) {
        LocalDate end = start.plus(REPEAT_PERIOD);

        Set<LocalDate> dates = switch (repeatType) {
            case NONE -> Set.of(start);
            case DAILY -> daily(start, end);
            case WEEKLY -> weekly(start, end, repeatWeekdays);
            case MONTHLY -> monthly(start, end, repeatDayOfMonth);
        };

        if (dates.isEmpty()) {
            throw new CustomException(TodoErrorCode.INVALID_REQUEST);
        }
        return new ArrayList<>(dates);
    }

    public boolean occursOn(Todo rule, LocalDate date) {
        if (date.isBefore(rule.getStartDate()) || date.isAfter(rule.getEndDate())) {
            return false;
        }

        return switch (rule.getRepeatType()) {
            case NONE -> date.equals(rule.getStartDate());
            case DAILY -> true;
            case WEEKLY -> matchesWeekly(date, rule.getRepeatWeekdays());
            case MONTHLY -> matchesMonthly(date, rule.getStartDate(), rule.getRepeatDayOfMonth());
        };
    }

    private boolean matchesWeekly(LocalDate date, List<Weekday> weekdays) {
        Set<DayOfWeek> targets = weekdays.stream()
                .map(DAY_OF_WEEKS::get)
                .collect(Collectors.toSet());
        return targets.contains(date.getDayOfWeek());
    }

    private boolean matchesMonthly(LocalDate date, LocalDate startDate, int dayOfMonth) {
        if (date.getDayOfMonth() != dayOfMonth) {
            return false;
        }
        // 시작 월의 지정 일자가 시작일보다 이전이면 그 달은 스킵
        if (date.getYear() == startDate.getYear()
                && date.getMonth() == startDate.getMonth()
                && dayOfMonth < startDate.getDayOfMonth()) {
            return false;
        }
        return true;
    }

    private Set<LocalDate> daily(LocalDate start, LocalDate end) {
        return streamDates(start, end)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<LocalDate> weekly(LocalDate start, LocalDate end, List<Weekday> weekdays) {
        Set<DayOfWeek> targets = weekdays.stream()
                .map(DAY_OF_WEEKS::get)
                .collect(Collectors.toSet());

        return streamDates(start, end)
                .filter(date -> targets.contains(date.getDayOfWeek()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<LocalDate> monthly(LocalDate start, LocalDate end, int dayOfMonth) {
        Set<LocalDate> dates = new LinkedHashSet<>();
        LocalDate firstMonth = firstApplicableMonth(start, dayOfMonth);

        for (LocalDate month = firstMonth; !month.isAfter(end); month = month.plusMonths(1)) {
            resolveDateInMonth(month, dayOfMonth)
                    .filter(date -> !date.isAfter(end))
                    .ifPresent(dates::add);
        }
        return dates;
    }

    private LocalDate firstApplicableMonth(LocalDate start, int dayOfMonth) {
        LocalDate startMonth = start.withDayOfMonth(1);
        if (dayOfMonth < start.getDayOfMonth()) {
            return startMonth.plusMonths(1);
        }
        return startMonth;
    }

    private Optional<LocalDate> resolveDateInMonth(LocalDate month, int dayOfMonth) {
        if (dayOfMonth > month.lengthOfMonth()) {
            return Optional.empty();
        }
        return Optional.of(month.withDayOfMonth(dayOfMonth));
    }

    private Stream<LocalDate> streamDates(LocalDate start, LocalDate end) {
        return start.datesUntil(end.plusDays(1));
    }
}
