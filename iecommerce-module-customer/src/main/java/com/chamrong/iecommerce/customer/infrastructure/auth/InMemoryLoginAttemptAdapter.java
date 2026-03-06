package com.chamrong.iecommerce.customer.infrastructure.auth;

import com.chamrong.iecommerce.customer.domain.auth.AccountState;
import com.chamrong.iecommerce.customer.domain.auth.port.LoginAttemptPort;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * In-memory fallback implementation of {@link LoginAttemptPort} used when Redis is not available.
 *
 * <p>State is process-local and non-persistent; suitable for local development and tests only.
 */
@Slf4j
@Component
public class InMemoryLoginAttemptAdapter implements LoginAttemptPort {

  private final ConcurrentMap<String, AccountState> store = new ConcurrentHashMap<>();

  @Override
  public AccountState getAccountState(String customerId) {
    return store.computeIfAbsent(customerId, id -> new AccountState(id, 0, null));
  }

  @Override
  public void saveAccountState(AccountState state) {
    store.put(state.getCustomerId(), state);
    log.debug(
        "Updated in-memory account state for customerId={} failures={} lockedUntil={}",
        state.getCustomerId(),
        state.getConsecutiveFailures(),
        state.getLockedUntil() != null ? state.getLockedUntil() : Instant.EPOCH);
  }
}
