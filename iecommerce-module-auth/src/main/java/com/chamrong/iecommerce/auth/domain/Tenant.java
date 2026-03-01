package com.chamrong.iecommerce.auth.domain;

import com.chamrong.iecommerce.common.domain.BaseDomainEntity;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tenant aggregate (pure domain — no JPA). Persistence uses TenantEntity in infrastructure. */
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Tenant extends BaseDomainEntity {

  private String code;
  private String name;
  private TenantPlan plan = TenantPlan.FREE;
  private TenantStatus status = TenantStatus.PENDING_VERIFICATION;
  private Instant trialEndsAt;
  private TenantPreferences preferences = new TenantPreferences();
  private TenantProvisioningStatus provisioningStatus = TenantProvisioningStatus.COMPLETED;
  private boolean enabled = true;

  public Tenant(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public void activate() {
    this.status = TenantStatus.ACTIVE;
    this.enabled = true;
  }

  public void suspend() {
    this.status = TenantStatus.SUSPENDED;
    this.enabled = false;
  }

  public void updatePlan(TenantPlan newPlan, Instant trialEndsAt) {
    this.plan = newPlan;
    this.trialEndsAt = trialEndsAt;
  }

  public void updateProvisioningStatus(TenantProvisioningStatus newStatus) {
    this.provisioningStatus = newStatus;
  }

  public void updateStatus(TenantStatus newStatus) {
    this.status = newStatus;
    this.enabled =
        newStatus == TenantStatus.ACTIVE
            || newStatus == TenantStatus.TRIAL
            || newStatus == TenantStatus.GRACE;
  }

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
    t.enabled =
        status == TenantStatus.ACTIVE
            || status == TenantStatus.TRIAL
            || status == TenantStatus.GRACE;
    return t;
  }

  public static Tenant signup(String code, String name, Instant trialEndsAt) {
    var t = new Tenant(code, name);
    t.plan = TenantPlan.FREE;
    t.status = TenantStatus.TRIAL;
    t.trialEndsAt = trialEndsAt;
    t.provisioningStatus = TenantProvisioningStatus.INITIAL;
    return t;
  }
}
