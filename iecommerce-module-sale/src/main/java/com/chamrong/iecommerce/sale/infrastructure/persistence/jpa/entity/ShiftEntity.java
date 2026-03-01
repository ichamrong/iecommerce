package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity;

import com.chamrong.iecommerce.sale.domain.model.Shift.ShiftStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "sales_shifts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ShiftEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private String tenantId;

  @Version private Long version;

  @Column(nullable = false)
  private String staffId;

  @Column(nullable = false)
  private String terminalId;

  @Column(nullable = false)
  private Instant startTime;

  private Instant endTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ShiftStatus status;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private Instant updatedAt;
}
