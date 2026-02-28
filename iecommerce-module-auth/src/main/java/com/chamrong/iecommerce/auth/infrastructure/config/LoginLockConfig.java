package com.chamrong.iecommerce.auth.infrastructure.config;

import com.chamrong.iecommerce.auth.domain.lock.LoginLockPolicy;
import com.chamrong.iecommerce.auth.domain.lock.LoginLockStore;
import com.chamrong.iecommerce.auth.infrastructure.lock.InMemoryLoginLockStore;
import com.chamrong.iecommerce.auth.infrastructure.lock.RedisLoginLockStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Spring configuration for the login lock subsystem.
 *
 * <p>Wires together:
 *
 * <ul>
 *   <li>The {@link LoginLockPolicy} (Strategy) — computed from {@link LoginLockProperties}
 *   <li>The {@link LoginLockStore} — selected by {@code auth.lock.store} property
 * </ul>
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(LoginLockProperties.class)
public class LoginLockConfig {

  /**
   * Configurable-schedule progressive lock policy.
   *
   * <p>Reads the step durations from {@link LoginLockProperties#durations()}. The Nth lock
   * (0-indexed from the threshold) maps to {@code durations[min(n, last)]}.
   */
  @Bean
  public LoginLockPolicy loginLockPolicy(final LoginLockProperties props) {
    return new LoginLockPolicy() {

      @Override
      public Duration lockDurationFor(final int failedAttempts) {
        if (failedAttempts < props.threshold()) {
          return Duration.ZERO;
        }
        // Escalation index: 0, 1, 2, …, capped at last defined duration
        final List<Integer> durations = props.durations();
        final int escalation = Math.min(failedAttempts - props.threshold(), durations.size() - 1);
        return Duration.ofSeconds(durations.get(escalation));
      }

      @Override
      public int lockThreshold() {
        return props.threshold();
      }
    };
  }

  // ── Store selection ──────────────────────────────────────────────────────

  /** Memory store — active when {@code auth.lock.store=memory} (or not set). */
  @Bean
  @ConditionalOnProperty(name = "auth.lock.store", havingValue = "memory", matchIfMissing = true)
  public LoginLockStore inMemoryLoginLockStore() {
    log.info(
        "[LoginLock] Using in-memory (Caffeine) lock store. "
            + "For production multi-node deployments, set auth.lock.store=redis.");
    return new InMemoryLoginLockStore();
  }

  /** Redis store — active when {@code auth.lock.store=redis}. */
  @Bean
  @ConditionalOnProperty(name = "auth.lock.store", havingValue = "redis")
  public LoginLockStore redisLoginLockStore(
      final StringRedisTemplate redisTemplate, final ObjectMapper objectMapper) {
    log.info("[LoginLock] Using Redis lock store.");
    return new RedisLoginLockStore(redisTemplate, objectMapper);
  }
}
