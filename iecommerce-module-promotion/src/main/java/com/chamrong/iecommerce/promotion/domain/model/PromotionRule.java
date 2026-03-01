package com.chamrong.iecommerce.promotion.domain.model;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "promotion_rule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromotionRule extends BaseTenantEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "promotion_id", nullable = false)
  private Promotion promotion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RuleType type;

  @Column(nullable = false)
  private String ruleData; // JSON or comma-separated configuration

  public Promotion getPromotion() {
    return promotion;
  }

  public RuleType getType() {
    return type;
  }

  public String getRuleData() {
    return ruleData;
  }

  // ── Factory Methods ──────────────────────────────────────────────────────

  public static PromotionRule create(Promotion promotion, RuleType type, String ruleData) {
    PromotionRule rule = new PromotionRule();
    rule.setTenantId(promotion.getTenantId());
    rule.promotion = promotion;
    rule.type = type;
    rule.ruleData = ruleData;
    return rule;
  }

  // ── Behavior Methods ─────────────────────────────────────────────────────

  public void update(RuleType type, String ruleData) {
    this.type = type;
    this.ruleData = ruleData;
  }
}
