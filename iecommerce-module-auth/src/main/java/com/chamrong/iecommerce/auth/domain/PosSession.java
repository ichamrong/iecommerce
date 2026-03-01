package com.chamrong.iecommerce.auth.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "auth_pos_session")
public class PosSession extends BaseTenantEntity {

  @Column(nullable = false)
  private Long terminalId;

  @Column(nullable = false)
  private Long cashierId;

  @Column(nullable = false)
  private Instant openedAt;

  private Instant closedAt;

  @Column(length = 255)
  private String closingNotes;

  @Column(nullable = false)
  private boolean active = true;

  public PosSession(String tenantId, Long terminalId, Long cashierId) {
    setTenantId(tenantId);
    this.terminalId = terminalId;
    this.cashierId = cashierId;
    this.openedAt = Instant.now();
  }

  public void closeSession(String notes) {
    if (!this.active) {
      throw new IllegalStateException("Session is already closed");
    }
    this.active = false;
    this.closedAt = Instant.now();
    this.closingNotes = notes;
  }
}
