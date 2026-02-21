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
@Table(name = "auth_user")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseTenantEntity {

  @Column(unique = true, nullable = false)
  private String username;

  @Column(unique = true, name = "keycloak_id")
  private String keycloakId;

  // Nullable since Keycloak will manage hashing and storage for SSO
  private String password;

  @Column(unique = true, nullable = false)
  private String email;

  private boolean enabled = true;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "auth_user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();
}
