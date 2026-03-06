package com.chamrong.iecommerce.customer.infrastructure.auth;

import com.chamrong.iecommerce.customer.application.dto.AuthTokens;
import com.chamrong.iecommerce.customer.domain.auth.port.CustomerCredentialPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Development-only implementation of {@link CustomerCredentialPort}.
 *
 * <p>For local testing, this adapter:
 *
 * <ul>
 *   <li>treats any non-empty password as valid
 *   <li>returns opaque, non-cryptographic token strings
 * </ul>
 *
 * <p>In production, replace this with a real identity provider (Keycloak or local password store).
 */
@Slf4j
@Component
@Profile({"dev", "local", "default"})
public class DevCustomerCredentialAdapter implements CustomerCredentialPort {

  @Override
  public boolean verify(String customerId, String password) {
    boolean valid = password != null && !password.isBlank();
    log.debug("Dev credential verification for customerId={} result={}", customerId, valid);
    return valid;
  }

  @Override
  public AuthTokens generateTokens(String customerId, long tokenVersion, String sessionId) {
    String accessToken = "dev-access-" + customerId + "-" + tokenVersion + "-" + sessionId;
    String refreshToken = "dev-refresh-" + customerId + "-" + tokenVersion + "-" + sessionId;
    return new AuthTokens(accessToken, refreshToken);
  }
}
