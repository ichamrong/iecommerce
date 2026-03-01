package com.chamrong.iecommerce.auth.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * User aggregate root for the auth domain.
 *
 * <h3>Design constraints</h3>
 *
 * <ul>
 *   <li>The {@code password} field is intentionally absent — Keycloak manages credential storage.
 *   <li>Composite indexes enforce uniqueness at DB level per tenant, not just application level.
 *   <li>{@link UserAccountState} tracks the account lifecycle via an explicit state column rather
 *       than a bare boolean {@code enabled} flag.
 * </ul>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "auth_user",
    indexes = {
      @Index(name = "idx_user_tenant_username", columnList = "tenant_id, username", unique = true),
      @Index(name = "idx_user_tenant_email", columnList = "tenant_id, email", unique = true),
      @Index(name = "idx_user_keycloak_id", columnList = "keycloak_id")
    })
public class User extends BaseTenantEntity {

  @Column(nullable = false, length = 150)
  private String username;

  @Column(unique = true, name = "keycloak_id", length = 36)
  private String keycloakId;

  @Column(nullable = false, length = 254)
  private String email;

  /**
   * Whether the account is allowed to log in.
   *
   * <p>Separate from {@link #accountState} — a SUSPENDED account sets this to {@code false}. Kept
   * for backwards compatibility with existing Keycloak sync logic.
   */
  private boolean enabled = true;

  /**
   * Full lifecycle state of the account.
   *
   * <p>This is the authoritative state. {@link #enabled} is kept in sync whenever this changes.
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "account_state", nullable = false, length = 20)
  private UserAccountState accountState = UserAccountState.ACTIVE;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "auth_user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"),
      indexes = @Index(name = "idx_user_roles_role_id", columnList = "role_id"))
  private Set<Role> roles = new HashSet<>();

  // ── Bootstrap ──────────────────────────────────────────────────────────────

  public User(String tenantId, String username, String email) {
    setTenantId(tenantId);
    this.username = username;
    this.email = email;
  }

  // ── State transitions ───────────────────────────────────────────────────────

  /** Transitions account from PENDING → ACTIVE (e.g., after first-login password reset). */
  public void activate() {
    this.accountState = UserAccountState.ACTIVE;
    this.enabled = true;
  }

  /** Sets account to PENDING — awaiting first-login password reset. */
  public void pendingActivation() {
    this.accountState = UserAccountState.PENDING;
    this.enabled = true; // login is allowed so first-login reset can happen
  }

  /** Permanently disables the account — blocks login without deleting data. */
  public void disable() {
    this.enabled = false;
  }

  public void suspend() {
    this.accountState = UserAccountState.SUSPENDED;
    this.enabled = false;
  }

  /** Soft-deletes the account — data retained for audit compliance. */
  public void softDelete() {
    this.accountState = UserAccountState.DELETED;
    this.enabled = false;
  }

  /** Returns {@code true} if the account is in a terminal deleted state. */
  public boolean isDeleted() {
    return this.accountState == UserAccountState.DELETED;
  }

  public void linkKeycloak(String keycloakId) {
    this.keycloakId = keycloakId;
  }

  public void addRole(Role role) {
    this.roles.add(role);
  }
}
