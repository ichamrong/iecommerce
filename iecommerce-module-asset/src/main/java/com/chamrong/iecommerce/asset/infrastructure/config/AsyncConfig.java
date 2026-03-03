package com.chamrong.iecommerce.asset.infrastructure.config;

import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration("assetAsyncConfig")
@EnableAsync
public class AsyncConfig {

  @Bean(name = "assetTaskExecutor")
  public Executor assetTaskExecutor() {
    log.info("Creating Async Task Executor for Asset Module");
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5); // Minimum threads
    executor.setMaxPoolSize(20); // Maximum threads when queue is full
    executor.setQueueCapacity(100); // Queue size for holding pending tasks
    executor.setThreadNamePrefix("AssetAsync-");
    executor.initialize();
    return executor;
  }
}
