package com.chamrong.iecommerce.auth.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Set<User> getUsers() {
    return users;
  }

  public void setUsers(Set<User> users) {
    this.users = users;
  }
}
