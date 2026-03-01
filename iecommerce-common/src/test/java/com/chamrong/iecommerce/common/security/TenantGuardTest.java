package com.chamrong.iecommerce.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chamrong.iecommerce.common.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

/** Tests for TenantGuard: requireTenantIdPresent and requireSameTenant. */
class TenantGuardTest {

  @AfterEach
  void clearContext() {
    TenantContext.clear();
  }

  @Test
  void requireTenantIdPresent_whenNoContext_throwsUnauthorized() {
    assertThatThrownBy(TenantGuard::requireTenantIdPresent)
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex ->
                assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(401));
  }

  @Test
  void requireTenantIdPresent_whenContextSet_returnsTenantId() {
    TenantContext.setCurrentTenant("tenant-1");
    assertThat(TenantGuard.requireTenantIdPresent()).isEqualTo("tenant-1");
  }

  @Test
  void requireSameTenant_whenMatch_doesNotThrow() {
    TenantGuard.requireSameTenant("tenant-A", "tenant-A");
  }

  @Test
  void requireSameTenant_whenCurrentNull_throwsUnauthorized() {
    assertThatThrownBy(() -> TenantGuard.requireSameTenant("tenant-A", null))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex ->
                assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(401));
  }

  @Test
  void requireSameTenant_whenEntityTenantDifferent_throwsNotFound() {
    assertThatThrownBy(() -> TenantGuard.requireSameTenant("tenant-A", "tenant-B"))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex ->
                assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(404));
  }

  @Test
  void requireSameTenant_whenEntityTenantNull_throwsNotFound() {
    assertThatThrownBy(() -> TenantGuard.requireSameTenant(null, "tenant-B"))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex ->
                assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(404));
  }
}
