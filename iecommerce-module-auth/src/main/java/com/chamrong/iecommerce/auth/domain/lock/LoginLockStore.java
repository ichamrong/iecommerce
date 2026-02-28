package com.chamrong.iecommerce.auth.domain.lock;

import java.util.Optional;

/**
 * Port interface for the username-level login lock store.
 *
 * <p>Implementations:
 *
 * <ul>
 *   <li>{@link com.chamrong.iecommerce.auth.infrastructure.lock.InMemoryLoginLockStore} — Caffeine,
 *       single-node, TTL-expired automatically.
 *   <li>{@link com.chamrong.iecommerce.auth.infrastructure.lock.RedisLoginLockStore} — Redis,
 *       distributed, TTL-expired via Redis {@code EXPIRE}. Use in production.
 * </ul>
 *
 * <h3>Thread safety</h3>
 *
 * <p>Both implementations must be safe under concurrent access from multiple threads. The Redis
 * implementation achieves this via atomic Lua scripts. The Caffeine implementation uses {@code
 * Cache.asMap()} which is backed by a ConcurrentHashMap.
 */
public interface LoginLockStore {

  /**
   * Returns the current attempt record for the given user+tenant, if any.
   *
   * @param username the login username
   * @param tenantId the tenant scope
   * @return the current record, or empty if no failed attempts have been recorded yet
   */
  Optional<LoginAttemptRecord> find(String username, String tenantId);

  /**
   * Atomically saves the given record, replacing any existing entry.
   *
   * <p>Implementations should set a TTL equal to the maximum possible lock duration + a small
   * buffer (e.g. 24 hours) so stale counts are self-cleaning.
   *
   * @param record the attempt record to persist
   */
  void save(LoginAttemptRecord record);

  /**
   * Removes all lock/attempt data for the given user+tenant.
   *
   * <p>Called after a successful login to reset the counter cleanly.
   *
   * @param username the login username
   * @param tenantId the tenant scope
   */
  void clear(String username, String tenantId);
}
