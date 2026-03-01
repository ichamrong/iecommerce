package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.sale.domain.model.SaleReturn.ReturnStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "sale_returns",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"tenant_id", "return_key"})})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SaleReturnEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private String tenantId;

  @Version private Long version;

  @Column(nullable = false)
  private Long originalOrderId;

  @Column(nullable = false)
  private String returnKey;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReturnStatus status;

  @Column(nullable = false)
  private String reason;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "total_refund_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  })
  private Money totalRefundAmount;

  @OneToMany(mappedBy = "saleReturn", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReturnItemEntity> items = new ArrayList<>();

  @Column(nullable = false)
  private Instant requestedAt;

  private Instant completedAt;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private Instant updatedAt;
}
