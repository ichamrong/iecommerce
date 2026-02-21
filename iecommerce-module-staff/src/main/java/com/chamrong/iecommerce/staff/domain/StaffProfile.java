package com.chamrong.iecommerce.staff.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
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

  /** Username of the corresponding User account in the auth module. */
  @Column(unique = true, nullable = false)
  private String userId;

  @Column(nullable = false)
  private String fullName;

  private String phone;

  private String department;

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

  private boolean active = true;
}
