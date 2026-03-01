package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sales_saga_states")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SaleSagaStateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String tenantId;

  @Column(nullable = false, unique = true)
  private String correlationId; // Usually the Quotation ID as String

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SagaStatus status;

  @Column(nullable = false)
  private String currentStep;

  @Version private Long version;

  private Instant lastUpdated;

  public enum SagaStatus {
    STARTED,
    ORDER_CREATED,
    STOCK_RESERVED,
    PAYMENT_INITIATED,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
  }

  public SaleSagaStateEntity(String tenantId, String correlationId) {
    this.tenantId = tenantId;
    this.correlationId = correlationId;
    this.status = SagaStatus.STARTED;
    this.currentStep = "START";
    this.lastUpdated = Instant.now();
  }

  public void updateStatus(SagaStatus status, String step) {
    this.status = status;
    this.currentStep = step;
    this.lastUpdated = Instant.now();
  }
}
