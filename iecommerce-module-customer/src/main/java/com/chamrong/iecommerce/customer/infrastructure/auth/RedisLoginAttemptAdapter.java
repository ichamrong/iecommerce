package com.chamrong.iecommerce.customer.infrastructure.auth;

import com.chamrong.iecommerce.customer.domain.auth.AccountState;
import com.chamrong.iecommerce.customer.domain.auth.port.LoginAttemptPort;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLoginAttemptAdapter implements LoginAttemptPort {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String KEY_PREFIX = "customer:auth:state:";
  private static final Duration TTL = Duration.ofHours(24);

  @Override
  public AccountState getAccountState(String customerId) {
    String key = KEY_PREFIX + customerId;
    try {
      AccountState state = (AccountState) redisTemplate.opsForValue().get(key);
      if (state != null) {
        return state;
      }
    } catch (Exception e) {
      log.error("Redis error fetching account state for {}: {}", customerId, e.getMessage(), e);
      // Fail open to allow login if Redis is down, but start with 0 failures
    }
    return new AccountState(customerId, 0, null);
  }

  @Override
  public void saveAccountState(AccountState state) {
    String key = KEY_PREFIX + state.getCustomerId();
    try {
      redisTemplate.opsForValue().set(key, state, TTL);
    } catch (Exception e) {
      log.error(
          "Redis error saving account state for {}: {}", state.getCustomerId(), e.getMessage(), e);
    }
  }
}
