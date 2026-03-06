package com.chamrong.iecommerce.common.domain;

import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Persistence-free base for domain aggregates. Use in domain layer only; JPA entities extend {@link
 * com.chamrong.iecommerce.common.BaseEntity} in infrastructure.
 *
 * <p>Setter methods are intended for persistence reconstitution and mapping only; domain logic
 * should prefer behavior methods over direct state mutation.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseDomainEntity {

  private Long id;
  private Instant createdAt;
  private Instant updatedAt;
  private boolean deleted = false;
  private Instant deletedAt;

  /** For reconstitution from persistence only; do not use in domain logic. */
  public void setId(Long id) {
    this.id = id;
  }

  /** For reconstitution from persistence only; do not use in domain logic. */
  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  /** For reconstitution from persistence only; do not use in domain logic. */
  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  /** For reconstitution from persistence only; do not use in domain logic. */
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  /** For reconstitution from persistence only; do not use in domain logic. */
  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }

  protected void softDelete() {
    this.deleted = true;
    this.deletedAt = Instant.now();
  }

  protected void restore() {
    this.deleted = false;
    this.deletedAt = null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BaseDomainEntity that = (BaseDomainEntity) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}


