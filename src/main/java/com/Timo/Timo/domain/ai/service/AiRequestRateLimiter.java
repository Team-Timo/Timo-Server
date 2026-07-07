package com.Timo.Timo.domain.ai.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import com.Timo.Timo.domain.ai.exception.AiErrorCode;
import com.Timo.Timo.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AiRequestRateLimiter {

	private static final String KEY_PREFIX = "ai:quota:";
	private static final int REQUEST_COST = 1;
	private static final DefaultRedisScript<Long> LIMIT_SCRIPT = createLimitScript();

	private final RedisTemplate<String, String> redisTemplate;

	@Value("${ai.rate-limit.user-rpm}")
	private int userRpmLimit;

	@Value("${ai.rate-limit.global-rpm}")
	private int globalRpmLimit;

	@Value("${ai.rate-limit.user-rpd}")
	private int userRpdLimit;

	@Value("${ai.rate-limit.global-rpd}")
	private int globalRpdLimit;

	@Value("${ai.rate-limit.global-tpm}")
	private int globalTpmLimit;

	public void validate(Long userId, int estimatedTokenCost) {
		String minuteBucket = currentMinuteBucket();
		String dayBucket = currentDayBucket();
		long minuteTtlSeconds = secondsUntilNextMinute();
		long dayTtlSeconds = secondsUntilNextDay();

		List<String> keys = List.of(
			KEY_PREFIX + "rpm:global:" + minuteBucket,
			KEY_PREFIX + "rpm:user:" + userId + ":" + minuteBucket,
			KEY_PREFIX + "rpd:global:" + dayBucket,
			KEY_PREFIX + "rpd:user:" + userId + ":" + dayBucket,
			KEY_PREFIX + "tpm:global:" + minuteBucket
		);

		Long allowed = redisTemplate.execute(
			LIMIT_SCRIPT,
			keys,
			String.valueOf(globalRpmLimit),
			String.valueOf(userRpmLimit),
			String.valueOf(globalRpdLimit),
			String.valueOf(userRpdLimit),
			String.valueOf(globalTpmLimit),
			String.valueOf(REQUEST_COST),
			String.valueOf(REQUEST_COST),
			String.valueOf(REQUEST_COST),
			String.valueOf(REQUEST_COST),
			String.valueOf(estimatedTokenCost),
			String.valueOf(minuteTtlSeconds),
			String.valueOf(dayTtlSeconds)
		);

		if (allowed == null || allowed == 0L) {
			throw new CustomException(AiErrorCode.AI_RATE_LIMIT_EXCEEDED);
		}
	}

	private static DefaultRedisScript<Long> createLimitScript() {
		DefaultRedisScript<Long> script = new DefaultRedisScript<>();
		script.setResultType(Long.class);
		script.setScriptText("""
			local keyCount = #KEYS
			local minuteTtl = tonumber(ARGV[11])
			local dayTtl = tonumber(ARGV[12])

			for i = 1, keyCount do
				local limit = tonumber(ARGV[i])
				local cost = tonumber(ARGV[keyCount + i])
				local current = tonumber(redis.call('GET', KEYS[i]) or '0')

				if limit <= 0 or current + cost > limit then
					return 0
				end
			end

			for i = 1, keyCount do
				local cost = tonumber(ARGV[keyCount + i])
				local ttl = minuteTtl

				if string.find(KEYS[i], ':rpd:') then
					ttl = dayTtl
				end

				redis.call('INCRBY', KEYS[i], cost)
				if redis.call('TTL', KEYS[i]) < 0 then
					redis.call('EXPIRE', KEYS[i], ttl)
				end
			end

			return 1
			""");
		return script;
	}

	private String currentMinuteBucket() {
		return LocalDateTime.now(ZoneOffset.UTC)
			.truncatedTo(ChronoUnit.MINUTES)
			.toString();
	}

	private String currentDayBucket() {
		return LocalDate.now(ZoneOffset.UTC).toString();
	}

	private long secondsUntilNextMinute() {
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime nextMinute = now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
		return Math.max(1, Duration.between(now, nextMinute).toSeconds());
	}

	private long secondsUntilNextDay() {
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime nextDay = now.toLocalDate().plusDays(1).atStartOfDay();
		return Math.max(1, Duration.between(now, nextDay).toSeconds());
	}
}
