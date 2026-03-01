package com.chamrong.iecommerce.sale.domain.model;

import com.chamrong.iecommerce.sale.domain.exception.SaleDomainException;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Shift aggregate. PURE Domain Model: No JPA/Spring annotations. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class Shift {

  private Long id;
  private String tenantId;
  private Long version;
  private String staffId;
  private String terminalId;
  private Instant startTime;
  private Instant endTime;
  private ShiftStatus status;

  /** Set by persistence layer for keyset pagination (created_at DESC, id DESC). */
  private Instant createdAt;

  public enum ShiftStatus {
    OPEN,
    CLOSED,
    SUSPENDED
  }

  public Shift(String tenantId, String staffId, String terminalId) {
    this.tenantId = tenantId;
    this.staffId = staffId;
    this.terminalId = terminalId;
    this.startTime = Instant.now();
    this.status = ShiftStatus.OPEN;
  }

  // Factory constructor for mapper
  public Shift(
      Long id,
      String tenantId,
      Long version,
      String staffId,
      String terminalId,
      Instant startTime,
      Instant endTime,
      ShiftStatus status,
      Instant createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.version = version;
    this.staffId = staffId;
    this.terminalId = terminalId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.status = status;
    this.createdAt = createdAt;
  }

  public void close() {
    if (this.status != ShiftStatus.OPEN && this.status != ShiftStatus.SUSPENDED) {
      throw new SaleDomainException("Shift is already " + this.status);
    }
    this.status = ShiftStatus.CLOSED;
    this.endTime = Instant.now();
    log.info("Shift {} closed for staff {} at terminal {}", this.id, this.staffId, this.terminalId);
  }

  public void suspend() {
    if (this.status != ShiftStatus.OPEN) {
      throw new SaleDomainException("Only OPEN shifts can be suspended");
    }
    this.status = ShiftStatus.SUSPENDED;
  }

  public void resume() {
    if (this.status != ShiftStatus.SUSPENDED) {
      throw new SaleDomainException("Only SUSPENDED shifts can be resumed");
    }
    this.status = ShiftStatus.OPEN;
  }
}
