package com.chamrong.iecommerce.auth.infrastructure.lock;

import com.chamrong.iecommerce.auth.domain.lock.LoginAttemptRecord;
import com.chamrong.iecommerce.auth.domain.lock.LoginLockStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis-backed implementation of {@link LoginLockStore}.
 *
 * <h3>Atomicity</h3>
 *
 * <p>Uses Redis {@code SET} with {@code EX} (atomic set + expire) for saves. This ensures no race
 * condition between writing the record and setting the TTL — critical in distributed environments
 * where two nodes may process the same user.
 *
 * <h3>Key design</h3>
 *
 * <pre>
 *   login:lock:{tenantId}:{username}  →  JSON(LoginAttemptRecord)
 *   TTL: max(lockDuration, 24h)
 * </pre>
 *
 * <p>The TTL is set to 24 hours: long enough to keep the counter for re-locking (if a user keeps
 * trying after the lock expires), short enough to self-clean.
 *
 * <h3>When to use</h3>
 *
 * <p>Always in production, especially when running multiple application instances. Requires {@code
 * spring-boot-starter-data-redis} on the classpath.
 */
@Slf4j
@RequiredArgsConstructor
public class RedisLoginLockStore implements LoginLockStore {

  private static final String KEY_PREFIX = "login:lock:";
  private static final long DEFAULT_TTL_HOURS = 24;

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public Optional<LoginAttemptRecord> find(final String username, final String tenantId) {
    final String json = redisTemplate.opsForValue().get(key(username, tenantId));
    if (json == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(objectMapper.readValue(json, LoginAttemptRecord.class));
    } catch (JsonProcessingException e) {
      log.error("Failed to deserialize lock record for user={} tenant={}", username, tenantId, e);
      return Optional.empty();
    }
  }

  /**
   * Saves atomically using Redis SET with EX.
   *
   * <p>The TTL is the longer of:
   *
   * <ul>
   *   <li>The remaining lock duration (so the record survives at least until unlock).
   *   <li>24 hours (so the failure count persists for re-locking after the lock expires).
   * </ul>
   */
  @Override
  public void save(final LoginAttemptRecord record) {
    try {
      final String json = objectMapper.writeValueAsString(record);
      final long ttlSeconds = computeTtlSeconds(record);
      redisTemplate
          .opsForValue()
          .set(key(record.username(), record.tenantId()), json, ttlSeconds, TimeUnit.SECONDS);
    } catch (JsonProcessingException e) {
      log.error(
          "Failed to serialize lock record for user={} tenant={}",
          record.username(),
          record.tenantId(),
          e);
    }
  }

  @Override
  public void clear(final String username, final String tenantId) {
    redisTemplate.delete(key(username, tenantId));
  }

  private static String key(final String username, final String tenantId) {
    return KEY_PREFIX + tenantId + ":" + username;
  }

  private static long computeTtlSeconds(final LoginAttemptRecord record) {
    final long defaultTtl = TimeUnit.HOURS.toSeconds(DEFAULT_TTL_HOURS);
    if (record.lockedUntil() == null) {
      return defaultTtl;
    }
    final long lockRemaining = Duration.between(Instant.now(), record.lockedUntil()).toSeconds();
    return Math.max(lockRemaining + 60, defaultTtl); // +60s buffer
  }
}
