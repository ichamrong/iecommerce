package com.chamrong.iecommerce.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link UserAccountFactory} covering the main account creation variants. */
class UserAccountFactoryTest {

  private static final String TENANT_ID = "TENANT-1";

  @Test
  void createSelfRegisteredShouldProduceActiveUserWithLowercasedIdentifiers() {
    User user = UserAccountFactory.createSelfRegistered(" Alice ", "ALICE@Example.com ", TENANT_ID);

    assertThat(user.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(user.getUsername()).isEqualTo("alice");
    assertThat(user.getEmail()).isEqualTo("alice@example.com");
    assertThat(user.getAccountState()).isEqualTo(UserAccountState.ACTIVE);
    assertThat(user.isEnabled()).isTrue();
  }

  @Test
  void createAdminInvitedShouldProducePendingUser() {
    User user = UserAccountFactory.createAdminInvited("Bob ", "Bob@example.com ", TENANT_ID);

    assertThat(user.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(user.getUsername()).isEqualTo("bob");
    assertThat(user.getEmail()).isEqualTo("bob@example.com");
    assertThat(user.getAccountState()).isEqualTo(UserAccountState.PENDING);
    // Pending accounts are allowed to log in to complete first-login reset
    assertThat(user.isEnabled()).isTrue();
  }

  @Test
  void createFromSocialLoginShouldProduceActiveUser() {
    User user = UserAccountFactory.createFromSocialLogin("Carol ", "CAROL@example.com ", TENANT_ID);

    assertThat(user.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(user.getUsername()).isEqualTo("carol");
    assertThat(user.getEmail()).isEqualTo("carol@example.com");
    assertThat(user.getAccountState()).isEqualTo(UserAccountState.ACTIVE);
    assertThat(user.isEnabled()).isTrue();
  }
}
