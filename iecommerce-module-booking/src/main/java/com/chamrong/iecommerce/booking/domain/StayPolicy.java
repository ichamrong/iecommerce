package com.chamrong.iecommerce.booking.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** Stay constraints and policies per date or default rules for a lodge. */
@Getter
@Setter
@Entity
@Table(name = "booking_stay_policy")
public class StayPolicy extends BaseTenantEntity {

  @Column(nullable = false)
  private Long resourceProductId;

  @Column(length = 100)
  private String label; // "Pchum Ben Season"

  // Date ranges where this rule applies. If null, it is the default overarching policy.
  @Column private LocalDate startDate;

  @Column private LocalDate endDate;

  @Column(nullable = false)
  private Integer minStay;

  @Column private Integer maxStay;
}
