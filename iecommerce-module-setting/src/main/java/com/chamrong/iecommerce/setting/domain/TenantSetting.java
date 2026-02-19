package com.chamrong.iecommerce.setting.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "setting_tenant")
public class TenantSetting extends BaseTenantEntity {

  @Column(name = "setting_key", nullable = false)
  private String key;

  @Column(name = "setting_value")
  private String value;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
