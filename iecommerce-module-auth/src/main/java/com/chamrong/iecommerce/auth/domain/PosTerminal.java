package com.chamrong.iecommerce.auth.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

  public PosTerminal(String tenantId, String name, String hardwareId, String branchId) {
    setTenantId(tenantId);
    this.name = name;
    this.hardwareId = hardwareId;
    this.branchId = branchId;
    this.pendingPairing = true;
  }

  public void activate() {
    this.active = true;
    this.pendingPairing = false;
  }

  public void deactivate() {
    this.active = false;
  }
}
