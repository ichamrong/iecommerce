package com.chamrong.iecommerce.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link User} state transitions and simple mutators. */
class UserTest {

  private static final String TENANT_ID = "TENANT-1";

  @Test
  void pendingActivationShouldSetPendingStateAndKeepEnabled() {
    User user = new User(TENANT_ID, "user", "user@example.com");

    user.pendingActivation();

    assertThat(user.getAccountState()).isEqualTo(UserAccountState.PENDING);
    assertThat(user.isEnabled()).isTrue();
  }

  @Test
  void activateShouldSetActiveStateAndEnable() {
    User user = new User(TENANT_ID, "user", "user@example.com");
    user.pendingActivation();

    user.activate();

    assertThat(user.getAccountState()).isEqualTo(UserAccountState.ACTIVE);
    assertThat(user.isEnabled()).isTrue();
  }

  @Test
  void suspendShouldDisableAndSetSuspendedState() {
    User user = new User(TENANT_ID, "user", "user@example.com");

    user.suspend();

    assertThat(user.getAccountState()).isEqualTo(UserAccountState.SUSPENDED);
    assertThat(user.isEnabled()).isFalse();
  }

  @Test
  void softDeleteShouldMarkDeletedAndDisable() {
    User user = new User(TENANT_ID, "user", "user@example.com");

    user.softDelete();

    assertThat(user.getAccountState()).isEqualTo(UserAccountState.DELETED);
    assertThat(user.isDeleted()).isTrue();
    assertThat(user.isEnabled()).isFalse();
  }

  @Test
  void disableShouldSetEnabledFalseWithoutChangingState() {
    User user = new User(TENANT_ID, "user", "user@example.com");
    UserAccountState initialState = user.getAccountState();

    user.disable();

    assertThat(user.isEnabled()).isFalse();
    assertThat(user.getAccountState()).isEqualTo(initialState);
  }

  @Test
  void linkKeycloakShouldSetKeycloakId() {
    User user = new User(TENANT_ID, "user", "user@example.com");

    user.linkKeycloak("kc-123");

    assertThat(user.getKeycloakId()).isEqualTo("kc-123");
  }

  @Test
  void addRoleShouldAddRoleToSet() {
    User user = new User(TENANT_ID, "user", "user@example.com");
    Role role = new Role(Role.ROLE_TENANT_ADMIN);

    user.addRole(role);

    assertThat(user.getRoles()).contains(role);
  }

  @Test
  void changeUsernameShouldUpdateUsername() {
    User user = new User(TENANT_ID, "user", "user@example.com");

    user.changeUsername("new-user");

    assertThat(user.getUsername()).isEqualTo("new-user");
  }

  @Test
  void markPasswordChangedShouldUpdateTimestamp() {
    User user = new User(TENANT_ID, "user", "user@example.com");
    Instant now = Instant.now();

    user.markPasswordChanged(now);

    assertThat(user.getLastPasswordChangeAt()).isEqualTo(now);
  }
}
