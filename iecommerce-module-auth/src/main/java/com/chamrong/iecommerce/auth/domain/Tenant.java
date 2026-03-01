package com.chamrong.iecommerce.auth.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "auth_tenant")
public class Tenant extends BaseEntity {

  /** Unique short identifier used as the tenantId in all scoped entities (e.g. "shop_a"). */
  @Column(unique = true, nullable = false)
  private String code;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TenantPlan plan = TenantPlan.FREE;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TenantStatus status = TenantStatus.PENDING_VERIFICATION;

  private Instant trialEndsAt;

  @Embedded private TenantPreferences preferences = new TenantPreferences();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TenantProvisioningStatus provisioningStatus = TenantProvisioningStatus.COMPLETED;

  private boolean enabled = true;

  public Tenant(String code, String name) {
    this.code = code;
    this.name = name;
  }

  // ── Domain behaviour ─────────────────────────────────────────────────────

  public void activate() {
    this.status = TenantStatus.ACTIVE;
    this.enabled = true;
  }

  public void suspend() {
    this.status = TenantStatus.DISABLED;
    this.enabled = false;
  }

  public void updatePlan(TenantPlan newPlan, Instant trialEndsAt) {
    this.plan = newPlan;
    this.trialEndsAt = trialEndsAt;
  }

  public void updateProvisioningStatus(TenantProvisioningStatus newStatus) {
    this.provisioningStatus = newStatus;
  }

  /** Updates lifecycle status, keeping enabled flag in sync. */
  public void updateStatus(TenantStatus newStatus) {
    this.status = newStatus;
    this.enabled = newStatus == TenantStatus.ACTIVE || newStatus == TenantStatus.TRIAL;
  }

  /** Factory for platform-admin provisioning (produces tenants with any plan/status). */
  public static Tenant provision(
      String code,
      String name,
      TenantPlan plan,
      TenantStatus status,
      TenantProvisioningStatus provisioningStatus) {
    var t = new Tenant(code, name);
    t.plan = plan;
    t.status = status;
    t.provisioningStatus = provisioningStatus;
    t.enabled = status == TenantStatus.ACTIVE || status == TenantStatus.TRIAL;
    return t;
  }

  /** Factory for self-service signup (FREE plan, TRIAL status with 30-day expiry). */
  public static Tenant signup(String code, String name, Instant trialEndsAt) {
    var t = new Tenant(code, name);
    t.plan = TenantPlan.FREE;
    t.status = TenantStatus.TRIAL;
    t.trialEndsAt = trialEndsAt;
    t.provisioningStatus = TenantProvisioningStatus.INITIAL;
    return t;
  }
}
