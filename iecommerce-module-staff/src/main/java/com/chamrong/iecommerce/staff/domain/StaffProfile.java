package com.chamrong.iecommerce.staff.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Platform-level staff member master data.
 *
 * <p>A staff member authenticates as a {@code User} (in the SYSTEM tenant), but their profile and
 * tenant assignment list lives here. The {@code userId} field is the link between the two.
 */
@Entity
@Table(name = "staff_profile")
@Getter
@Setter
@NoArgsConstructor
public class StaffProfile extends BaseEntity {

  public StaffProfile(String userId, String fullName, StaffRole role) {
    this.userId = userId;
    this.fullName = fullName;
    this.role = role != null ? role : StaffRole.SUPPORT;
    this.status = StaffStatus.ACTIVE;
    this.hireDate = LocalDate.now();
  }

  /** Username of the corresponding User account in the auth module. */
  @Column(unique = true, nullable = false)
  private String userId;

  @Column(nullable = false)
  private String fullName;

  private String phone;
  private String department;
  private String branch;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StaffStatus status = StaffStatus.ACTIVE;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StaffRole role = StaffRole.SUPPORT;

  private LocalDate hireDate;
  private LocalDate terminationDate;

  /**
   * Tenant codes this staff member is allowed to manage.
   *
   * <p>This list is embedded in the JWT {@code assignedTenants} claim, enabling stateless
   * enforcement — no DB lookup required per request.
   */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "staff_assigned_tenants", joinColumns = @JoinColumn(name = "staff_id"))
  @Column(name = "tenant_code")
  private Set<String> assignedTenants = new HashSet<>();

  // ── Domain Methods ────────────────────────────────────────────────────────

  public void suspend() {
    this.status = StaffStatus.SUSPENDED;
  }

  public void reactivate() {
    if (this.status == StaffStatus.TERMINATED) {
      throw new IllegalStateException("Cannot reactivate a terminated staff member.");
    }
    this.status = StaffStatus.ACTIVE;
  }

  public void terminate() {
    this.status = StaffStatus.TERMINATED;
    this.terminationDate = LocalDate.now();
  }
}
