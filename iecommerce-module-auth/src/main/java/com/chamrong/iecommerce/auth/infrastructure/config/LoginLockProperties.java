package com.chamrong.iecommerce.auth.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the username-level login lock.
 *
 * <h3>application.yml example</h3>
 *
 * <pre>
 * auth:
 *   lock:
 *     store: memory          # 'memory' (Caffeine) or 'redis'
 *     threshold: 3           # failed attempts before first lock
 *     durations:
 *       - 60                 # lock duration (seconds) for threshold+0 attempts
 *       - 180                # lock duration for threshold+2 attempts
 *       - 300                # lock duration for threshold+4+ attempts
 * </pre>
 *
 * @param store which backing store to use: {@code memory} or {@code redis}
 * @param threshold number of consecutive failures before the first lock is applied
 * @param durations list of lock durations (seconds) indexed by lock escalation level: index 0 =
 *     first lock, index 1 = second lock, index N = Nth+ lock
 */
@ConfigurationProperties(prefix = "auth.lock")
public record LoginLockProperties(String store, int threshold, java.util.List<Integer> durations) {

  /** Default: memory store, lock after 3 failures, durations 60/180/300 seconds. */
  public LoginLockProperties {
    if (store == null) store = "memory";
    if (threshold <= 0) threshold = 3;
    if (durations == null || durations.isEmpty()) {
      durations = java.util.List.of(60, 180, 300);
    }
  }
}
