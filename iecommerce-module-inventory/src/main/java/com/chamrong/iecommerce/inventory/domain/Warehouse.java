package com.chamrong.iecommerce.inventory.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inventory_warehouse")
public class Warehouse extends BaseTenantEntity {

  @Column(nullable = false, length = 255)
  private String name;

  @Column(length = 255)
  private String location;
}
