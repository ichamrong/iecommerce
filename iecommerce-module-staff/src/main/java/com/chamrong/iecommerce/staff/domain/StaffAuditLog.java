package com.chamrong.iecommerce.staff.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Audit log recording all actions performed regarding a staff member. */
@Entity
@Table(
    name = "staff_audit_log",
    indexes = {
      @Index(name = "idx_audit_staff", columnList = "target_staff_id"),
      @Index(name = "idx_audit_actor", columnList = "actor_id")
    })
@Getter
@NoArgsConstructor
public class StaffAuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String actorId;

  @Column(nullable = false)
  private Long targetStaffId;

  @Column(nullable = false)
  private String actionType;

  @Column(nullable = false)
  private Instant timestamp;

  public StaffAuditLog(String actorId, Long targetStaffId, String actionType) {
    this.actorId = actorId;
    this.targetStaffId = targetStaffId;
    this.actionType = actionType;
    this.timestamp = Instant.now();
  }
}
