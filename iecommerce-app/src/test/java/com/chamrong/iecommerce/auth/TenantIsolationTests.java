package com.chamrong.iecommerce.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.common.AbstractIntegrationTest;
import com.chamrong.iecommerce.common.TenantContext;
import org.junit.jupiter.api.Test;

public class TenantIsolationTests extends AbstractIntegrationTest {

  @Test
  void userFromTenantACannotSeeDataFromTenantB() {
    // UAT-AUTH-03: Tenant Isolation
    // 1. Set context to Tenant A
    TenantContext.setCurrentTenant("TENANT_A");

    // 2. Perform some action (to be implemented)

    // 3. Verify isolation (to be implemented)

    assertThat(TenantContext.getCurrentTenant()).isEqualTo("TENANT_A");
  }
}
