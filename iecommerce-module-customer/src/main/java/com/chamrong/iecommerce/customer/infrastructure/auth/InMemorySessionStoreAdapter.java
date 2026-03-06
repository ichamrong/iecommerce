package com.chamrong.iecommerce.customer.infrastructure.auth;

import com.chamrong.iecommerce.customer.domain.auth.port.SessionStorePort;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * In-memory fallback implementation of {@link SessionStorePort} used when Redis is not available.
 *
 * <p>Session data is kept in-memory only and is lost on restart; suitable for local development.
 */
@Slf4j
@Component
public class InMemorySessionStoreAdapter implements SessionStorePort {

  // customerId → (sessionId → deviceMetadata)
  private final Map<String, Map<String, String>> sessions = new ConcurrentHashMap<>();

  @Override
  public void registerSession(String customerId, String sessionId, String deviceMetadata) {
    sessions
        .computeIfAbsent(customerId, id -> new ConcurrentHashMap<>())
        .put(sessionId, deviceMetadata);
    log.debug("Registered in-memory session {} for customer {}", sessionId, customerId);
  }

  @Override
  public void invalidateOtherSessions(String customerId, String currentSessionId) {
    Map<String, String> byCustomer = sessions.get(customerId);
    if (byCustomer == null) return;
    Set<String> keys = Set.copyOf(byCustomer.keySet());
    for (String sid : keys) {
      if (!sid.equals(currentSessionId)) {
        byCustomer.remove(sid);
      }
    }
    log.debug("Invalidated other in-memory sessions for customer {}", customerId);
  }

  @Override
  public void invalidateAll(String customerId) {
    sessions.remove(customerId);
    log.debug("Invalidated all in-memory sessions for customer {}", customerId);
  }
}
