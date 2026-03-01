package com.chamrong.iecommerce.auth.infrastructure.persistence.jpa.entity;

import com.chamrong.iecommerce.auth.domain.TenantPlan;
import com.chamrong.iecommerce.auth.domain.TenantProvisioningStatus;
import com.chamrong.iecommerce.auth.domain.TenantStatus;
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

/** JPA entity for Tenant. Domain model is {@link com.chamrong.iecommerce.auth.domain.Tenant}. */
@Entity
@Table(name = "auth_tenant")
@Getter
@Setter
@NoArgsConstructor
public class TenantEntity extends BaseEntity {

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

  @Column(name = "trial_ends_at")
  private Instant trialEndsAt;

  @Embedded private TenantPreferencesEmbeddable preferences = new TenantPreferencesEmbeddable();

  @Enumerated(EnumType.STRING)
  @Column(name = "provisioning_status", nullable = false)
  private TenantProvisioningStatus provisioningStatus = TenantProvisioningStatus.COMPLETED;

  @Column(nullable = false)
  private boolean enabled = true;
}
