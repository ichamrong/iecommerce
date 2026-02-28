package com.chamrong.iecommerce.customer.application.command;

import com.chamrong.iecommerce.customer.domain.Customer;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import com.chamrong.iecommerce.customer.domain.auth.port.SessionStorePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordHandler {

  private final CustomerRepository customerRepository;
  private final SessionStorePort sessionStorePort;

  @Transactional
  public void handle(ResetPasswordCommand cmd) {
    Customer customer =
        customerRepository
            .findByEmail(cmd.email())
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    // In a real system, verify reset token and update password in Auth module/Keycloak here
    log.info("Resetting password for customer: {}", cmd.email());

    // 1. Increment token version for stateless JWT revocation
    customer.incrementTokenVersion();
    customerRepository.save(customer);

    // 2. Clear stateful Redis sessions
    sessionStorePort.invalidateAll(customer.getId().toString());

    log.info(
        "REVOKED_ALL_SESSIONS: Customer={}, new TokenVersion={}",
        cmd.email(),
        customer.getTokenVersion());
  }
}
