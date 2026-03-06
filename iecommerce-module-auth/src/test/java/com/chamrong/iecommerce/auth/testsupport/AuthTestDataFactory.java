package com.chamrong.iecommerce.auth.testsupport;

import com.chamrong.iecommerce.auth.domain.PosSession;
import com.chamrong.iecommerce.auth.domain.PosTerminal;
import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.User;

/**
 * Small factory for creating commonly used auth-domain test objects.
 *
 * <p>Intentionally minimal; grows organically as more tests need shared fixtures.
 */
public final class AuthTestDataFactory {

  private static final String DEFAULT_TENANT_ID = "TENANT-1";

  private AuthTestDataFactory() {}

  public static Tenant tenant(String code) {
    return new Tenant(code, "Tenant " + code);
  }

  public static User user(String username) {
    return new User(DEFAULT_TENANT_ID, username, username + "@example.com");
  }

  public static PosTerminal terminal(String tenantId) {
    return new PosTerminal(tenantId, "Terminal-1", "HW-1", "BR-1");
  }

  public static PosSession activeSession(String tenantId, Long terminalId, Long cashierId) {
    return new PosSession(tenantId, terminalId, cashierId);
  }
}
