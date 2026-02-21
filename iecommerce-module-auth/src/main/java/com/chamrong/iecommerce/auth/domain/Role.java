package com.chamrong.iecommerce.auth.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auth_role")
@Getter
@Setter
@NoArgsConstructor
public class Role extends BaseTenantEntity {

  public static final String ROLE_PLATFORM_ADMIN = "ROLE_PLATFORM_ADMIN";
  public static final String ROLE_ACCOUNTING = "ROLE_ACCOUNTING";
  public static final String ROLE_MODERATOR = "ROLE_MODERATOR";
  public static final String ROLE_SYSTEM_STATUS = "ROLE_SYSTEM_STATUS";
  public static final String ROLE_TENANT_ADMIN = "ROLE_TENANT_ADMIN";
  public static final String ROLE_PLATFORM_STAFF = "ROLE_PLATFORM_STAFF";
  public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";

  @Column(unique = true, nullable = false)
  private String name;

  private String description;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "auth_role_permissions",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private Set<Permission> permissions = new HashSet<>();

  public Role(String name) {
    this.name = name;
  }
}
