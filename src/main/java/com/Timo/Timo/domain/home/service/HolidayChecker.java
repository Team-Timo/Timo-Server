package com.Timo.Timo.domain.home.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.home.client.HolidayApiClient;
import com.Timo.Timo.domain.user.enums.Language;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HolidayChecker {

	private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
	private static final int MIN_SUPPORTED_YEAR = 2004;
	private static final int MAX_FUTURE_YEARS = 2;
	private static final Duration LOAD_WAIT = Duration.ofSeconds(2);
	private static final Duration RETRY_DELAY = Duration.ofMinutes(10);

	private final HolidayApiClient holidayApiClient;
	private final Executor holidayExecutor;
	private final Map<Integer, Set<LocalDate>> koreanHolidaysByYear = new ConcurrentHashMap<>();
	private final Map<Integer, LoadingYear> loadingYears = new ConcurrentHashMap<>();
	private final Map<Integer, Instant> failedAtByYear = new ConcurrentHashMap<>();

	public HolidayChecker(
			HolidayApiClient holidayApiClient,
			@Qualifier("holidayExecutor") Executor holidayExecutor
	) {
		this.holidayApiClient = holidayApiClient;
		this.holidayExecutor = holidayExecutor;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void loadHolidaysOnStartup() {
		if (!holidayApiClient.isConfigured()) {
			log.warn("Holiday API key is not configured.");
			return;
		}

		int currentYear = LocalDate.now(KOREA_ZONE).getYear();
		startLoading(currentYear - 1);
		startLoading(currentYear);
		startLoading(currentYear + 1);
	}

	@Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
	public void refreshUpcomingHolidays() {
		if (!holidayApiClient.isConfigured()) {
			return;
		}

		int currentYear = LocalDate.now(KOREA_ZONE).getYear();
		startLoading(currentYear);
		startLoading(currentYear + 1);
	}

	public boolean isHoliday(LocalDate date, Language language) {
		if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
			return true;
		}

		if (language != Language.KO) {
			return false;
		}

		Set<LocalDate> koreanHolidays = findKoreanHolidays(date.getYear());
		return koreanHolidays != null && koreanHolidays.contains(date);
	}

	private Set<LocalDate> findKoreanHolidays(int year) {
		Set<LocalDate> cached = koreanHolidaysByYear.get(year);
		if (cached != null || !isLoadable(year)) {
			return cached;
		}

		LoadingYear loading = startLoading(year);
		long waitMillis = Duration.between(Instant.now(), loading.waitDeadline()).toMillis();
		if (waitMillis <= 0) {
			return null;
		}

		try {
			return loading.future().get(waitMillis, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		} catch (ExecutionException | TimeoutException e) {
			return null;
		}
	}

	private boolean isLoadable(int year) {
		int currentYear = LocalDate.now(KOREA_ZONE).getYear();
		if (!holidayApiClient.isConfigured() || year < MIN_SUPPORTED_YEAR || year > currentYear + MAX_FUTURE_YEARS) {
			return false;
		}

		Instant failedAt = failedAtByYear.get(year);
		return failedAt == null || failedAt.plus(RETRY_DELAY).isBefore(Instant.now());
	}

	private LoadingYear startLoading(int year) {
		return loadingYears.computeIfAbsent(year, key -> new LoadingYear(
				CompletableFuture.supplyAsync(() -> loadHolidays(key), holidayExecutor),
				Instant.now().plus(LOAD_WAIT)
		));
	}

	private Set<LocalDate> loadHolidays(int year) {
		try {
			Set<LocalDate> holidays = Set.copyOf(holidayApiClient.fetchHolidays(year));
			if (holidays.isEmpty()) {
				failedAtByYear.put(year, Instant.now());
				return null;
			}
			koreanHolidaysByYear.put(year, holidays);
			failedAtByYear.remove(year);
			return holidays;
		} catch (Exception e) {
			failedAtByYear.put(year, Instant.now());
			log.warn("Failed to fetch holidays. year={}", year, e);
			return null;
		} finally {
			loadingYears.remove(year);
		}
	}

	private record LoadingYear(
			CompletableFuture<Set<LocalDate>> future,
			Instant waitDeadline
	) {
	}
}
