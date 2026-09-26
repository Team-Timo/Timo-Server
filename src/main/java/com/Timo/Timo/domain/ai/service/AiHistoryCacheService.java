package com.Timo.Timo.domain.ai.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.Timo.Timo.domain.ai.dto.TodoDurationHistory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiHistoryCacheService {

  private static final String HISTORY_VERSION_KEY = "ai:history:version:";
  private static final String SIMILAR_KEY_PREFIX = "ai:history:similar:";
  private static final String TAG_KEY_PREFIX = "ai:history:tag:";
  private static final Duration HISTORY_CACHE_TTL = Duration.ofMinutes(5);
  private static final TypeReference<List<TodoDurationHistory>> HISTORY_LIST_TYPE = new TypeReference<>() {
  };

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  public CacheLookupResult getSimilarTitleHistories(
      Long userId,
      String title,
      LocalDateTime toExclusive,
      ZoneId userZoneId,
      int limit
  ) {
    return getHistories(buildSimilarKey(userId, title, toExclusive, userZoneId, limit));
  }

  public CacheLookupResult getRecentTagHistories(
      Long userId,
      Long tagId,
      LocalDateTime toExclusive,
      ZoneId userZoneId,
      int limit
  ) {
    return getHistories(buildTagKey(userId, tagId, toExclusive, userZoneId, limit));
  }

  public void cacheHistories(String key, List<TodoDurationHistory> histories) {
    try {
      redisTemplate.opsForValue().set(
          key,
          objectMapper.writeValueAsString(histories),
          HISTORY_CACHE_TTL
      );
    } catch (Exception exception) {
      log.warn("Failed to serialize AI history cache. key={}", key, exception);
    }
  }

  public void bumpUserHistoryVersion(Long userId) {
    try {
      redisTemplate.opsForValue().increment(HISTORY_VERSION_KEY + userId);
    } catch (Exception exception) {
      log.warn("Failed to bump AI history cache version. userId={}", userId, exception);
    }
  }

  private CacheLookupResult getHistories(String key) {
    String value;
    try {
      value = redisTemplate.opsForValue().get(key);
    } catch (Exception exception) {
      log.warn("Failed to read AI history cache, treating as miss. key={}", key, exception);
      return CacheLookupResult.miss(key);
    }

    if (value == null || value.isBlank()) {
      return CacheLookupResult.miss(key);
    }

    try {
      return CacheLookupResult.hit(objectMapper.readValue(value, HISTORY_LIST_TYPE), key);
    } catch (Exception exception) {
      log.warn("Failed to deserialize AI history cache. key={}", key, exception);
      redisTemplate.delete(key);
      return CacheLookupResult.miss(key);
    }
  }

  private String buildSimilarKey(
      Long userId,
      String title,
      LocalDateTime toExclusive,
      ZoneId userZoneId,
      int limit
  ) {
    String normalizedTitle = title == null ? "" : title.trim().toLowerCase();
    return SIMILAR_KEY_PREFIX
        + userId
        + ":v" + getUserHistoryVersion(userId)
        + ":" + normalizedTitle.hashCode()
        + ":" + toExclusive
        + ":" + userZoneId.getId()
        + ":" + limit;
  }

  private String buildTagKey(
      Long userId,
      Long tagId,
      LocalDateTime toExclusive,
      ZoneId userZoneId,
      int limit
  ) {
    return TAG_KEY_PREFIX
        + userId
        + ":v" + getUserHistoryVersion(userId)
        + ":" + tagId
        + ":" + toExclusive
        + ":" + userZoneId.getId()
        + ":" + limit;
  }

  private long getUserHistoryVersion(Long userId) {
    try {
      String value = redisTemplate.opsForValue().get(HISTORY_VERSION_KEY + userId);
      if (value == null || value.isBlank()) {
        return 0L;
      }
      return Long.parseLong(value);
    } catch (Exception exception) {
      log.warn("Failed to read AI history cache version, defaulting to 0. userId={}", userId, exception);
      return 0L;
    }
  }

  public record CacheLookupResult(boolean hit, List<TodoDurationHistory> histories, String key) {

    private static CacheLookupResult hit(List<TodoDurationHistory> histories, String key) {
      return new CacheLookupResult(true, histories, key);
    }

    private static CacheLookupResult miss(String key) {
      return new CacheLookupResult(false, List.of(), key);
    }
  }
}