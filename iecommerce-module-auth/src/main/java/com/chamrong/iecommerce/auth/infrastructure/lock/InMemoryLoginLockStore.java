package com.chamrong.iecommerce.auth.infrastructure.lock;

import com.chamrong.iecommerce.auth.domain.lock.LoginAttemptRecord;
import com.chamrong.iecommerce.auth.domain.lock.LoginLockStore;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Optional;

/**
 * Caffeine-backed in-memory implementation of {@link LoginLockStore}.
 *
 * <h3>Thread safety</h3>
 *
 * <p>Caffeine's {@code Cache.asMap()} is backed by a {@code ConcurrentHashMap} internally. However,
 * the read-modify-write sequence in {@link #save} could be non-atomic under high concurrency. This
 * is acceptable for a single-node scenario — missed increments are bounded (an attacker needs only
 * slightly more attempts to trigger the lock). For atomic guarantees, use {@link
 * RedisLoginLockStore}.
 *
 * <h3>TTL / memory</h3>
 *
 * <p>Entries expire 24 hours after last write. Maximum 100,000 entries (~100k concurrent attacker
 * IPs) before Caffeine evicts LRU entries. Both limits are configurable via {@link
 * com.chamrong.iecommerce.auth.infrastructure.config.LoginLockProperties}.
 *
 * <h3>When to use</h3>
 *
 * <p>Development, testing, and single-instance deployments. For multi-instance production use
 * {@link RedisLoginLockStore}.
 */
public class InMemoryLoginLockStore implements LoginLockStore {

  /**
   * Max record lifetime — set to typical lock duration + comfortable buffer. Even after all lock
   * phases expire, we keep the count for re-locking on further attempts.
   */
  private static final Duration RECORD_TTL = Duration.ofHours(24);

  private static final long MAX_ENTRIES = 100_000;

  private final Cache<String, LoginAttemptRecord> cache;

  public InMemoryLoginLockStore() {
    this.cache =
        Caffeine.newBuilder().expireAfterWrite(RECORD_TTL).maximumSize(MAX_ENTRIES).build();
  }

  @Override
  public Optional<LoginAttemptRecord> find(final String username, final String tenantId) {
    return Optional.ofNullable(cache.getIfPresent(key(username, tenantId)));
  }

  /**
   * Saves the record. Not strictly atomic under high concurrency — acceptable for single-node. See
   * class Javadoc for details.
   */
  @Override
  public void save(final LoginAttemptRecord record) {
    cache.put(key(record.username(), record.tenantId()), record);
  }

  @Override
  public void clear(final String username, final String tenantId) {
    cache.invalidate(key(username, tenantId));
  }

  private static String key(final String username, final String tenantId) {
    // Prefix with "lock:" to namespace away from other Caffeine usages
    return "lock:" + tenantId + ":" + username;
  }
}
