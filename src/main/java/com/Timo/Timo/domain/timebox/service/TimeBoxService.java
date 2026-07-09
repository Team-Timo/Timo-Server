package com.Timo.Timo.domain.timebox.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.timebox.dto.response.TimeBoxResponse;
import com.Timo.Timo.domain.timebox.enums.TimeBoxAction;
import com.Timo.Timo.domain.timebox.support.TimeBoxDateParser;
import com.Timo.Timo.domain.timer.entity.TimerRecord;
import com.Timo.Timo.domain.timer.entity.TimerSession;
import com.Timo.Timo.domain.timer.repository.TimerSessionRepository;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeBoxService {

	private static final int SECONDS_PER_MINUTE = 60;

	private final TimerSessionRepository timerSessionRepository;
	private final UserRepository userRepository;
	private final TimeBoxDateParser dateParser;

	public List<TimeBoxResponse> getTimeBoxes(Long userId, String dateValue) {
		LocalDate date = dateParser.parse(dateValue);
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
		ZoneId userZoneId = ZoneId.of(user.getZoneId());

		LocalDateTime dayStart = date.atStartOfDay();
		LocalDateTime nextDayStart = date.plusDays(1).atStartOfDay();
		LocalDateTime fromInclusiveUtc = toUtc(dayStart, userZoneId);
		LocalDateTime toExclusiveUtc = toUtc(nextDayStart, userZoneId);
		LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

		return timerSessionRepository.findTimeBoxSessions(
				userId,
				fromInclusiveUtc,
				toExclusiveUtc,
				nowUtc
			).stream()
			.map(session -> toResponse(session, date, dayStart, nextDayStart, userZoneId, nowUtc))
			.toList();
	}

	private TimeBoxResponse toResponse(
		TimerSession session,
		LocalDate date,
		LocalDateTime dayStart,
		LocalDateTime nextDayStart,
		ZoneId userZoneId,
		LocalDateTime nowUtc
	) {
		TimerRecord record = session.getTimerRecord();
		LocalDateTime localStartedAt = toUserTime(session.getStartedAt(), userZoneId);
		LocalDateTime startedAt = localStartedAt.isBefore(dayStart) ? dayStart : localStartedAt;
		LocalDateTime endedAt = resolveEndedAt(session, date, nextDayStart, userZoneId, nowUtc);
		TimeBoxAction startAction = resolveStartAction(session, record, localStartedAt, dayStart, nextDayStart);
		TimeBoxAction endAction = resolveEndAction(session, record, userZoneId, dayStart, nextDayStart);

		return new TimeBoxResponse(
			session.getId(),
			record.getId(),
			record.getTodo().getId(),
			record.getTodo().getTitle(),
			date,
			startedAt,
			startAction,
			endedAt,
			endAction,
			toActualMinutes(record, endAction)
		);
	}

	private LocalDateTime resolveEndedAt(
		TimerSession session,
		LocalDate date,
		LocalDateTime nextDayStart,
		ZoneId userZoneId,
		LocalDateTime nowUtc
	) {
		if (session.getPausedAt() != null) {
			LocalDateTime localEndedAt = toUserTime(session.getPausedAt(), userZoneId);
			return localEndedAt.isAfter(nextDayStart) ? nextDayStart : localEndedAt;
		}

		LocalDate today = toUserTime(nowUtc, userZoneId).toLocalDate();
		return date.isBefore(today) ? nextDayStart : null;
	}

	private TimeBoxAction resolveStartAction(
		TimerSession session,
		TimerRecord record,
		LocalDateTime localStartedAt,
		LocalDateTime dayStart,
		LocalDateTime nextDayStart
	) {
		if (localStartedAt.isBefore(dayStart) || !localStartedAt.isBefore(nextDayStart)) {
			return null;
		}
		return session.getStartedAt().equals(record.getStartedAt())
			? TimeBoxAction.START
			: TimeBoxAction.RESUME;
	}

	private TimeBoxAction resolveEndAction(
		TimerSession session,
		TimerRecord record,
		ZoneId userZoneId,
		LocalDateTime dayStart,
		LocalDateTime nextDayStart
	) {
		if (session.getPausedAt() == null) {
			return null;
		}

		LocalDateTime localPausedAt = toUserTime(session.getPausedAt(), userZoneId);
		if (localPausedAt.isBefore(dayStart) || localPausedAt.isAfter(nextDayStart)) {
			return null;
		}

		return session.getPausedAt().equals(record.getEndedAt())
			? TimeBoxAction.COMPLETE
			: TimeBoxAction.PAUSE;
	}

	private Integer toActualMinutes(TimerRecord record, TimeBoxAction endAction) {
		if (endAction != TimeBoxAction.COMPLETE || record.getActualSeconds() == null) {
			return null;
		}
		return record.getActualSeconds() / SECONDS_PER_MINUTE;
	}

	private LocalDateTime toUtc(LocalDateTime localDateTime, ZoneId userZoneId) {
		return localDateTime
			.atZone(userZoneId)
			.withZoneSameInstant(ZoneOffset.UTC)
			.toLocalDateTime();
	}

	private LocalDateTime toUserTime(LocalDateTime utcDateTime, ZoneId userZoneId) {
		return utcDateTime
			.atZone(ZoneOffset.UTC)
			.withZoneSameInstant(userZoneId)
			.toLocalDateTime();
	}
}