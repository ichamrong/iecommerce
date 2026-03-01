package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.sale.domain.model.SaleSession.SessionStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "sales_sessions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SaleSessionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private String tenantId;

  @Version private Long version;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shift_id", nullable = false)
  private ShiftEntity shift;

  @Column(nullable = false)
  private String cashierId;

  @Column(nullable = false)
  private String terminalId;

  private Instant startTime;
  private Instant endTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SessionStatus status;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "expected_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  })
  private Money expectedAmount;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "actual_amount")),
    @AttributeOverride(
        name = "currency",
        column = @Column(name = "currency", insertable = false, updatable = false))
  })
  private Money actualAmount;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private Instant updatedAt;
}
