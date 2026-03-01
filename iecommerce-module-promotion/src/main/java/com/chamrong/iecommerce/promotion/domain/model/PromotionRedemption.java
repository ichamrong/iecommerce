package com.chamrong.iecommerce.promotion.domain.model;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.promotion.domain.exception.PromotionDomainException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "promotion_redemption",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_redemption_key",
          columnNames = {"tenant_id", "redemption_key"})
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromotionRedemption extends BaseTenantEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "promotion_id", nullable = false)
  private Promotion promotion;

  @Column(nullable = false)
  private String orderId;

  @Column(nullable = false)
  private String customerId;

  @Column(name = "redemption_key", nullable = false)
  private String redemptionKey; // Idempotency key

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RedemptionStatus status;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  private Instant appliedAt;

  // ── Factory Methods ──────────────────────────────────────────────────────

  public static PromotionRedemption reserve(
      Promotion promotion,
      String tenantId,
      String orderId,
      String customerId,
      String redemptionKey,
      BigDecimal amount) {

    PromotionRedemption r = new PromotionRedemption();
    r.setTenantId(tenantId);
    r.promotion = promotion;
    r.orderId = orderId;
    r.customerId = customerId;
    r.amount = amount;
    r.redemptionKey = redemptionKey;
    r.status = RedemptionStatus.RESERVED;

    return r;
  }

  // ── Behavior Methods ─────────────────────────────────────────────────────

  public void apply() {
    if (this.status != RedemptionStatus.RESERVED) {
      throw new PromotionDomainException("Can only apply reserved redemptions");
    }
    this.status = RedemptionStatus.APPLIED;
    this.appliedAt = Instant.now();
  }

  public void release() {
    if (this.status == RedemptionStatus.RELEASED) return;
    this.status = RedemptionStatus.RELEASED;
  }

  // Manual getters
  public Promotion getPromotion() {
    return promotion;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public String getRedemptionKey() {
    return redemptionKey;
  }

  public RedemptionStatus getStatus() {
    return status;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Instant getAppliedAt() {
    return appliedAt;
  }
}
