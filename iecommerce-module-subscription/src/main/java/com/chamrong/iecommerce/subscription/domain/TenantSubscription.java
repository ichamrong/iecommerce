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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}
