package com.chamrong.iecommerce.auth.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

  public void closeSession(String notes) {
    if (!this.active) {
      throw new IllegalStateException("Session is already closed");
    }
    this.active = false;
    this.closedAt = Instant.now();
    this.closingNotes = notes;
  }
}
