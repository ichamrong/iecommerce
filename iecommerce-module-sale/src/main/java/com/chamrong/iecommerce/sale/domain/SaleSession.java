package com.chamrong.iecommerce.sale.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sales_sessions")
@Getter
@Setter
public class SaleSession extends BaseTenantEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shift_id")
  private Shift shift;

  private String customerId;

  private Long orderId;

  private Instant startTime;
  private Instant endTime;

  @Enumerated(EnumType.STRING)
  private SessionStatus status;

  @Column(length = 255)
  private String reference; // Table number, room number, etc.

  public enum SessionStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED,
    INVOICE_FAILED
  }
}
