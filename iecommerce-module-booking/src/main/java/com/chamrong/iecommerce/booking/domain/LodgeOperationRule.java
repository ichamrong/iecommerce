package com.chamrong.iecommerce.booking.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/** Operational rules and check-in configuration for a lodge (accommodation product). */
@Getter
@Setter
@Entity
@Table(name = "booking_lodge_operation_rule")
public class LodgeOperationRule extends BaseTenantEntity {

  @Column(nullable = false, unique = true)
  private Long resourceProductId;

  @Column(nullable = false)
  private LocalTime checkInTime;

  @Column(nullable = false)
  private LocalTime checkOutTime;

  @Column(nullable = false)
  private boolean earlyCheckInAllowed;

  /** Grace period in minutes for early check-in. */
  @Column private Integer earlyCheckInGracePeriodMins;

  /** Delivery method for check-in: Self Check-in, Meet Owner, Smart Lock, Staff. */
  @Column(length = 50)
  private String checkInMethod;

  /** Encrypted or hidden arrival instructions, only shown to confirmed guests. */
  @Column(columnDefinition = "TEXT")
  private String arrivalDetails;
}
