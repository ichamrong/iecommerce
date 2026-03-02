package com.chamrong.iecommerce.customer.application.command;

import com.chamrong.iecommerce.customer.application.dto.AuthTokens;
import com.chamrong.iecommerce.customer.domain.Customer;
import com.chamrong.iecommerce.customer.domain.auth.AccountState;
import com.chamrong.iecommerce.customer.domain.auth.ConcurrentLoginPolicy;
import com.chamrong.iecommerce.customer.domain.auth.LoginLockPolicy;
import com.chamrong.iecommerce.customer.domain.auth.port.CustomerCredentialPort;
import com.chamrong.iecommerce.customer.domain.auth.port.LoginAttemptPort;
import com.chamrong.iecommerce.customer.domain.auth.port.SessionStorePort;
import com.chamrong.iecommerce.customer.domain.ports.CustomerRepositoryPort;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginCustomerHandler {

  private final CustomerRepositoryPort customerRepository;
  private final CustomerCredentialPort credentialPort;
  private final LoginAttemptPort attemptPort;
  private final SessionStorePort sessionStorePort;
  private final LoginLockPolicy lockPolicy;

  // Ideally this is injected via @Value, defaulting to INVALIDATE_OLD for now
  private final ConcurrentLoginPolicy concurrentPolicy = ConcurrentLoginPolicy.INVALIDATE_OLD;

  @Transactional
  public AuthTokens handle(LoginCommand cmd) {
    Customer customer =
        customerRepository
            .findByTenantIdAndEmail(cmd.tenantId(), cmd.username())
            .orElseThrow(() -> new RuntimeException("Bad credentials")); // Obscure existence

    String customerId = customer.getId().toString();
    AccountState state = attemptPort.getAccountState(customerId);

    if (state.isLocked(Instant.now())) {
      log.warn("LOGIN_LOCKED: username={}", cmd.username());
      throw new RuntimeException("Account locked until " + state.getLockedUntil());
    }

    boolean isValid = credentialPort.verify(customerId, cmd.password());

    if (!isValid) {
      state.registerFailure(lockPolicy, Instant.now());
      attemptPort.saveAccountState(state);
      log.warn("LOGIN_FAILED: username={}", cmd.username());
      throw new RuntimeException("Bad credentials");
    }

    state.registerSuccess();
    attemptPort.saveAccountState(state);

    String newSessionId = UUID.randomUUID().toString();
    if (concurrentPolicy == ConcurrentLoginPolicy.INVALIDATE_OLD) {
      sessionStorePort.invalidateOtherSessions(customerId, newSessionId);
      log.info("SESSION_CONFLICT: Invalidated old sessions for {}", cmd.username());
    }

    sessionStorePort.registerSession(customerId, newSessionId, cmd.deviceMeta());
    log.info("LOGIN_SUCCESS: username={}", cmd.username());

    return credentialPort.generateTokens(customerId, customer.getTokenVersion(), newSessionId);
  }
}
