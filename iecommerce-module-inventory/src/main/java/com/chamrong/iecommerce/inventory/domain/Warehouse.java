package com.chamrong.iecommerce.inventory.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_warehouse")
public class Warehouse extends BaseTenantEntity {

  @Column(nullable = false, length = 255)
  private String name;

  @Column(length = 255)
  private String location;

  public Warehouse() {}

  public static Warehouse of(String tenantId, String name, String location) {
    var w = new Warehouse();
    w.setTenantId(tenantId);
    w.name = name;
    w.location = location;
    return w;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void rename(String newName) {
    this.name = newName;
  }

  public void updateLocation(String newLocation) {
    this.location = newLocation;
  }
}
