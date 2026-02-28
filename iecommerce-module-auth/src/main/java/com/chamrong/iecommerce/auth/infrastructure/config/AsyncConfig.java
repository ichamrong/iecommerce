package com.chamrong.iecommerce.auth.infrastructure.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Async execution configuration for the auth module.
 *
 * <h3>Why a dedicated thread pool?</h3>
 *
 * <p>Auth operations (email sends, OTP dispatch, TOTP setup) must not block the HTTP request
 * thread. Without a dedicated pool, async tasks compete with all other Spring {@code @Async} tasks
 * in the global pool, creating unpredictable latency spikes under load.
 *
 * <h3>Sizing rationale</h3>
 *
 * <ul>
 *   <li>Core = 4: handles steady-state OTP/email volume without idle threads.
 *   <li>Max = 20: handles burst scenarios (e.g. mass password-reset campaign).
 *   <li>Queue = 500: absorbs brief spikes; tasks beyond 500 use CallerRunsPolicy (caller thread
 *       executes — graceful degradation, not task loss).
 * </ul>
 */
@EnableAsync
@Configuration
public class AsyncConfig {

  /**
   * Dedicated executor for auth async operations. Referenced by name in
   * {@code @Async("authTaskExecutor")}.
   */
  @Bean("authTaskExecutor")
  public Executor authTaskExecutor() {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("auth-async-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);
    executor.initialize();
    return executor;
  }
}
