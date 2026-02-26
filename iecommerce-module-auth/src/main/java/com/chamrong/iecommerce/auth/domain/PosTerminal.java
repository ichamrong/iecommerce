package com.chamrong.iecommerce.auth.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "auth_pos_terminal")
public class PosTerminal extends BaseTenantEntity {

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 50, unique = true)
  private String hardwareId;

  @Column(length = 50)
  private String branchId;

  @Column(nullable = false)
  private boolean active = true;

  @Column(nullable = false)
  private boolean pendingPairing = false;

  public void activate() {
    this.active = true;
    this.pendingPairing = false;
  }

  public void deactivate() {
    this.active = false;
  }
}
