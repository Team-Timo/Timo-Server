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

  public void cacheSimilarTitleHistories(
      Long userId,
      String title,
      LocalDateTime toExclusive,
      ZoneId userZoneId,
      int limit,
      List<TodoDurationHistory> histories
  ) {
    cacheHistories(buildSimilarKey(userId, title, toExclusive, userZoneId, limit), histories);
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

  public void cacheRecentTagHistories(
      Long userId,
      Long tagId,
      LocalDateTime toExclusive,
      ZoneId userZoneId,
      int limit,
      List<TodoDurationHistory> histories
  ) {
    cacheHistories(buildTagKey(userId, tagId, toExclusive, userZoneId, limit), histories);
  }

  public void bumpUserHistoryVersion(Long userId) {
    redisTemplate.opsForValue().increment(HISTORY_VERSION_KEY + userId);
  }

  private CacheLookupResult getHistories(String key) {
    String value = redisTemplate.opsForValue().get(key);
    if (value == null || value.isBlank()) {
      return CacheLookupResult.miss();
    }

    try {
      return CacheLookupResult.hit(objectMapper.readValue(value, HISTORY_LIST_TYPE));
    } catch (Exception exception) {
      log.warn("Failed to deserialize AI history cache. key={}", key, exception);
      redisTemplate.delete(key);
      return CacheLookupResult.miss();
    }
  }

  private void cacheHistories(String key, List<TodoDurationHistory> histories) {
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
    String value = redisTemplate.opsForValue().get(HISTORY_VERSION_KEY + userId);
    if (value == null || value.isBlank()) {
      return 0L;
    }
    return Long.parseLong(value);
  }

  public record CacheLookupResult(boolean hit, List<TodoDurationHistory> histories) {

    private static CacheLookupResult hit(List<TodoDurationHistory> histories) {
      return new CacheLookupResult(true, histories);
    }

    private static CacheLookupResult miss() {
      return new CacheLookupResult(false, List.of());
    }
  }
}