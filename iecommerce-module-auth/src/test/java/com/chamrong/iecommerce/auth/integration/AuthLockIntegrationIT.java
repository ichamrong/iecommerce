package com.chamrong.iecommerce.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.auth.domain.lock.LoginLockPolicy;
import com.chamrong.iecommerce.auth.domain.lock.LoginLockStore;
import com.chamrong.iecommerce.auth.infrastructure.config.LoginLockConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Lightweight integration test to verify that the login lock subsystem is wired correctly via
 * Spring Boot configuration (policy + in-memory store).
 */
@SpringBootTest(
    classes = AuthLockIntegrationIT.AuthLockTestConfig.class,
    properties = {
      "auth.lock.store=memory",
      "auth.lock.threshold=3",
      "auth.lock.durations[0]=60",
      "auth.lock.durations[1]=180",
      "auth.lock.durations[2]=300"
    })
class AuthLockIntegrationIT {

  @SpringBootConfiguration
  @Import(LoginLockConfig.class)
  static class AuthLockTestConfig {}

  @Autowired private LoginLockPolicy policy;

  @Autowired private LoginLockStore store;

  @Test
  void contextShouldWirePolicyAndStoreBeans() {
    assertThat(policy).isNotNull();
    assertThat(store).isNotNull();
    assertThat(policy.lockThreshold()).isEqualTo(3);
  }
}
