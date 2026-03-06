package com.chamrong.iecommerce.subscription.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "subscription_plan")
public class SubscriptionPlan extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "price_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
  })
  private Money price;

  /** Quota limits stored as JSON or simplified fields. For now, let's use simplified fields. */
  private int maxProducts;

  private int maxVariants;

  private int maxOrdersPerMonth;
  private int maxStaffProfiles;
  private boolean customDomainAllowed;

  @Column(nullable = false)
  private boolean active = true;
}
