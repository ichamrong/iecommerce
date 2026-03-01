package com.chamrong.iecommerce.promotion.domain.model;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.promotion.domain.exception.PromotionDomainException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "promotion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Promotion extends BaseTenantEntity {

  @Column(nullable = false)
  private String name;

  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PromotionType type;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal value; // Legacy/Metadata amount

  @Column(columnDefinition = "TEXT")
  private String ruleJson; // Dynamic rule logic

  private String schemaVersion = "v1";

  @Column(unique = true)
  private String code;

  private Instant validFrom;
  private Instant validTo;

  @Version private Long version;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PromotionStatus status;

  @Column(nullable = false)
  private int priority = 0;

  @Column(nullable = false)
  private boolean stackable = false;

  @Column(nullable = false)
  private boolean active = true;

  private Integer usageLimit;
  private Integer usedCount = 0;

  // Manual getters to avoid Lombok issues in some environments
  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public PromotionType getType() {
    return type;
  }

  public BigDecimal getValue() {
    return value;
  }

  public String getCode() {
    return code;
  }

  public Instant getValidFrom() {
    return validFrom;
  }

  public Instant getValidTo() {
    return validTo;
  }

  public PromotionStatus getStatus() {
    return status;
  }

  public int getPriority() {
    return priority;
  }

  public boolean isStackable() {
    return stackable;
  }

  public Integer getUsageLimit() {
    return usageLimit;
  }

  public Integer getUsedCount() {
    return usedCount;
  }

  public String getRuleJson() {
    return ruleJson;
  }

  public List<PromotionRule> getRules() {
    return rules != null ? rules : new ArrayList<>();
  }

  @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
  @BatchSize(size = 10)
  private List<PromotionRule> rules = new ArrayList<>();

  // ── Factory Methods ──────────────────────────────────────────────────────

  public static Promotion create(
      String tenantId,
      String name,
      String description,
      PromotionType type,
      BigDecimal value,
      String code,
      Instant validFrom,
      Instant validTo,
      int priority,
      boolean stackable,
      Integer usageLimit) {

    Promotion p = new Promotion();
    p.setTenantId(tenantId);
    p.name = name;
    p.description = description;
    p.type = type;
    p.value = value;
    p.code = code;
    p.validFrom = validFrom;
    p.validTo = validTo;
    p.priority = priority;
    p.stackable = stackable;
    p.usageLimit = usageLimit;
    p.status = PromotionStatus.DRAFT;
    p.active = true;
    p.usedCount = 0;
    return p;
  }

  // ── Behavior Methods ─────────────────────────────────────────────────────

  public void activate() {
    if (this.status == PromotionStatus.ARCHIVED) {
      throw new PromotionDomainException("Cannot activate archived promotion");
    }
    this.status = PromotionStatus.ACTIVE;
    this.active = true;
  }

  public void pause() {
    this.status = PromotionStatus.PAUSED;
    this.active = false;
  }

  public void archive() {
    this.status = PromotionStatus.ARCHIVED;
    this.active = false;
  }

  public boolean isEligibleAt(Instant time) {
    if (status != PromotionStatus.ACTIVE) return false;
    if (validFrom != null && time.isBefore(validFrom)) return false;
    if (validTo != null && time.isAfter(validTo)) return false;
    if (usageLimit != null && usedCount >= usageLimit) return false;
    return true;
  }

  public void recordRedemption() {
    if (usageLimit != null && usedCount >= usageLimit) {
      throw new PromotionDomainException("Usage limit exceeded for promotion: " + code);
    }
    this.usedCount++;
    if (usageLimit != null && usedCount >= usageLimit) {
      this.status = PromotionStatus.EXPIRED;
    }
  }

  public void updateDetails(
      String name,
      String description,
      PromotionType type,
      BigDecimal value,
      String code,
      Instant validFrom,
      Instant validTo,
      int priority,
      boolean stackable,
      Integer usageLimit) {
    this.name = name;
    this.description = description;
    this.type = type;
    this.value = value;
    this.code = code;
    this.validFrom = validFrom;
    this.validTo = validTo;
    this.priority = priority;
    this.stackable = stackable;
    this.usageLimit = usageLimit;
  }
}
