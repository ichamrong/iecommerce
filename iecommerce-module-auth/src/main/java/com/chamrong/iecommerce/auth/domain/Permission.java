package com.chamrong.iecommerce.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** A fine-grained capability that can be assigned to a {@link Role}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "auth_permission")
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Dot/colon-namespaced capability name, e.g. {@code user:read}, {@code user:disable}. */
  @Column(unique = true, nullable = false)
  private String name;

  public Permission(String name) {
    this.name = name;
  }
}
