package com.chamrong.iecommerce.customer.infrastructure.auth;

import com.chamrong.iecommerce.customer.domain.auth.port.SessionStorePort;
import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnBean(RedisTemplate.class)
@RequiredArgsConstructor
public class RedisSessionStoreAdapter implements SessionStorePort {

  private final RedisTemplate<String, Object> redisTemplate;

  // Key structure: customer:auth:session:{customerId}:{sessionId} = deviceMetadata
  private static final String KEY_PREFIX = "customer:auth:session:";
  private static final Duration SESSION_TTL = Duration.ofDays(7);

  @Override
  public void registerSession(String customerId, String sessionId, String deviceMetadata) {
    String key = buildKey(customerId, sessionId);
    try {
      redisTemplate.opsForValue().set(key, deviceMetadata, SESSION_TTL);
    } catch (Exception e) {
      log.error(
          "Failed to register session {} for customer {}: {}",
          sessionId,
          customerId,
          e.getMessage(),
          e);
    }
  }

  @Override
  public void invalidateOtherSessions(String customerId, String currentSessionId) {
    String pattern = KEY_PREFIX + customerId + ":*";
    try {
      Set<String> keys = redisTemplate.keys(pattern);
      if (keys != null) {
        String currentSessionKey = buildKey(customerId, currentSessionId);
        keys.remove(currentSessionKey);

        if (!keys.isEmpty()) {
          redisTemplate.delete(keys);
          log.info("Invalidated {} old sessions for customer {}", keys.size(), customerId);
        }
      }
    } catch (Exception e) {
      log.error(
          "Failed to invalidate old sessions for customer {}: {}", customerId, e.getMessage(), e);
    }
  }

  @Override
  public void invalidateAll(String customerId) {
    String pattern = KEY_PREFIX + customerId + ":*";
    try {
      Set<String> keys = redisTemplate.keys(pattern);
      if (keys != null && !keys.isEmpty()) {
        redisTemplate.delete(keys);
        log.info("Invalidated ALL ({}) sessions for customer {}", keys.size(), customerId);
      }
    } catch (Exception e) {
      log.error(
          "Failed to invalidate all sessions for customer {}: {}", customerId, e.getMessage(), e);
    }
  }

  private String buildKey(String customerId, String sessionId) {
    return KEY_PREFIX + customerId + ":" + sessionId;
  }
}
