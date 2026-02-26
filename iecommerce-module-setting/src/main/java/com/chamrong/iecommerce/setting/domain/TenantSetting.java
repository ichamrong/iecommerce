package com.chamrong.iecommerce.setting.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Tenant-specific configuration entry.
 *
 * <p>A unique constraint on {@code (tenant_id, setting_key)} ensures that each tenant can have at
 * most one value per key, while different tenants can independently configure the same setting.
 *
 * <p>Examples: {@code store_name}, {@code base_currency}, {@code smtp_password}.
 */
@Getter
@Setter
@Entity
@Table(
    name = "setting_tenant",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "setting_key"}))
public class TenantSetting extends BaseTenantEntity {

  @Column(name = "setting_key", nullable = false, length = 100)
  private String key;

  @Column(name = "setting_value", columnDefinition = "TEXT")
  private String value;

  @Column(name = "description", length = 500)
  private String description;

  /** Category for grouped retrieval (e.g., GENERAL, EMAIL, QUOTA). */
  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false, length = 50)
  private SettingCategory category = SettingCategory.GENERAL;

  /** Expected data type — used by clients to safely deserialize the value. */
  @Enumerated(EnumType.STRING)
  @Column(name = "data_type", nullable = false, length = 20)
  private SettingDataType dataType = SettingDataType.STRING;

  /**
   * When {@code true} the value must be masked in API responses. Applies to credentials, tokens,
   * and passwords that must never be leaked in plaintext over the wire.
   */
  @Column(name = "is_secret", nullable = false)
  private boolean secret = false;
}
