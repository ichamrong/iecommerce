package com.chamrong.iecommerce.auth.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "auth_group")
public class Group extends BaseTenantEntity {

  @Column(nullable = false)
  private String name;

  private String description;

  @ManyToMany
  @JoinTable(
      name = "auth_group_users",
      joinColumns = @JoinColumn(name = "group_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  private Set<User> users = new HashSet<>();
}
