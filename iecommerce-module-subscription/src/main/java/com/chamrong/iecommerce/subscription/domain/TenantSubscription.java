package com.chamrong.iecommerce.subscription.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tenant_subscription")
public class TenantSubscription extends BaseTenantEntity {

  @ManyToOne
  @JoinColumn(name = "plan_id", nullable = false)
  private SubscriptionPlan plan;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private SubscriptionStatus status = SubscriptionStatus.TRIAL;

  @Column(nullable = false)
  private Instant startDate;

  private Instant endDate;

  private Instant lastBillingDate;
  private Instant nextBillingDate;

  @Column(nullable = false)
  private boolean autoRenew = true;

  // ── Factory ───────────────────────────────────────────────────────────────

  public static TenantSubscription startTrial(
      String tenantId, SubscriptionPlan plan, int trialDays) {
    var sub = new TenantSubscription();
    sub.setTenantId(tenantId);
    sub.plan = plan;
    sub.status = SubscriptionStatus.TRIAL;
    sub.startDate = Instant.now();
    sub.endDate = sub.startDate.plusSeconds((long) trialDays * 86400);
    sub.nextBillingDate = sub.endDate;
    sub.autoRenew = true;
    return sub;
  }

  // ── Domain behaviour ───────────────────────────────────────────────────────

  public boolean isActive() {
    return (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIAL)
        && (endDate == null || endDate.isAfter(Instant.now()));
  }

  public void cancel() {
    this.autoRenew = false;
    this.status = SubscriptionStatus.CANCELLED;
  }

  public void activate() {
    this.status = SubscriptionStatus.ACTIVE;
  }

  public void expire() {
    this.status = SubscriptionStatus.EXPIRED;
  }

  public void upgradeTo(SubscriptionPlan newPlan, Instant nextBillingDate) {
    this.plan = newPlan;
    this.status = SubscriptionStatus.ACTIVE;
    this.nextBillingDate = nextBillingDate;
  }

  /**
   * Resumes auto-renewal for a subscription that was previously cancelled or suspended.
   *
   * <p>Business rules: if the subscription is already expired, resuming is not allowed at the
   * domain level and should be handled by creating a new subscription instead.
   */
  public void resume() {
    if (status == SubscriptionStatus.EXPIRED) {
      throw new IllegalStateException("Cannot resume an expired subscription.");
    }
    this.autoRenew = true;
    if (status == SubscriptionStatus.CANCELLED || status == SubscriptionStatus.SUSPENDED) {
      this.status = SubscriptionStatus.ACTIVE;
    }
  }
}
