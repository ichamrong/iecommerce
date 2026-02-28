package com.chamrong.iecommerce.auth.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auth_tenant")
@Getter
@Setter
@NoArgsConstructor
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
}
