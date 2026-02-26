package com.chamrong.iecommerce.sale.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sales_shifts")
@Getter
@Setter
public class Shift extends BaseTenantEntity {

  @Column(nullable = false)
  private String staffId;

  @Column(nullable = false)
  private String terminalId;

  @Column(nullable = false)
  private Instant startTime;

  private Instant endTime;

  @Column(nullable = false)
  private BigDecimal openingBalance;

  private BigDecimal closingBalance;

  private BigDecimal expectedBalance;

  @Enumerated(EnumType.STRING)
  private ShiftStatus status;

  public enum ShiftStatus {
    OPEN,
    CLOSED,
    RECONCILED
  }
}
