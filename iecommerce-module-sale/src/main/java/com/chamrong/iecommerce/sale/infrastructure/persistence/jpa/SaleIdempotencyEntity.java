package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sales_idempotency")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SaleIdempotencyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "idempotency_key", nullable = false)
  private String idempotencyKey;

  @Column(name = "endpoint_name", nullable = false)
  private String endpointName;

  @Column(name = "request_hash", nullable = false)
  private String requestHash;

  @Column(name = "response_snapshot", nullable = false, columnDefinition = "TEXT")
  private String responseSnapshot;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public SaleIdempotencyEntity(
      String tenantId,
      String idempotencyKey,
      String endpointName,
      String requestHash,
      String responseSnapshot) {
    this.tenantId = tenantId;
    this.idempotencyKey = idempotencyKey;
    this.endpointName = endpointName;
    this.requestHash = requestHash;
    this.responseSnapshot = responseSnapshot;
    this.createdAt = Instant.now();
  }
}
