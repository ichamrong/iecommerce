package com.chamrong.iecommerce.sale.domain.model;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.sale.domain.exception.SaleDomainException;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** SaleSession aggregate. PURE Domain Model: No JPA/Spring annotations. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class SaleSession {

  private Long id;
  private String tenantId;
  private Long version;
  private Shift shift;
  private String cashierId;
  private String terminalId;
  private Instant startTime;
  private Instant endTime;
  private SessionStatus status;
  private Money expectedAmount;
  private Money actualAmount;

  /** Set by persistence layer for keyset pagination (created_at DESC, id DESC). */
  private Instant createdAt;

  public enum SessionStatus {
    OPEN,
    CLOSING,
    CLOSED
  }

  public SaleSession(Shift shift, String tenantId, String terminalId, String currency) {
    if (shift.getStatus() != Shift.ShiftStatus.OPEN) {
      throw new SaleDomainException("Cannot open a session for a non-open shift");
    }
    this.shift = shift;
    this.tenantId = tenantId;
    this.cashierId = shift.getStaffId();
    this.terminalId = terminalId;
    this.startTime = Instant.now();
    this.status = SessionStatus.OPEN;
    this.expectedAmount = Money.zero(currency);
  }

  // Factory constructor for mapper
  public SaleSession(
      Long id,
      String tenantId,
      Long version,
      Shift shift,
      String cashierId,
      String terminalId,
      Instant startTime,
      Instant endTime,
      SessionStatus status,
      Money expectedAmount,
      Money actualAmount,
      Instant createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.version = version;
    this.shift = shift;
    this.cashierId = cashierId;
    this.terminalId = terminalId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.status = status;
    this.expectedAmount = expectedAmount;
    this.actualAmount = actualAmount;
    this.createdAt = createdAt;
  }

  public void recordSale(Money amount) {
    if (this.status != SessionStatus.OPEN) {
      throw new SaleDomainException("Cannot record sale in session with status " + this.status);
    }
    this.expectedAmount = this.expectedAmount.add(amount);
  }

  public void initiateClosing() {
    if (this.status != SessionStatus.OPEN) {
      throw new SaleDomainException("Session must be OPEN to initiate closing");
    }
    this.status = SessionStatus.CLOSING;
  }

  public void close(Money actualCash) {
    if (this.status != SessionStatus.CLOSING) {
      throw new SaleDomainException("Session must be in CLOSING state before final closure");
    }
    if (!actualCash.getCurrency().equals(expectedAmount.getCurrency())) {
      throw new SaleDomainException("Currency mismatch in reconciliation");
    }
    this.status = SessionStatus.CLOSED;
    this.actualAmount = actualCash;
    this.endTime = Instant.now();
  }
}
