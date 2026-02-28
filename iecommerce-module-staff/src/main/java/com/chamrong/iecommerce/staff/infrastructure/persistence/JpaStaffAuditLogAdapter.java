package com.chamrong.iecommerce.staff.infrastructure.persistence;

import com.chamrong.iecommerce.staff.domain.StaffAuditLog;
import com.chamrong.iecommerce.staff.domain.StaffAuditLogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/** Spring Data backing repository for StaffAuditLog. */
@Repository
interface SpringDataStaffAuditLogRepository extends JpaRepository<StaffAuditLog, Long> {}

/** JPA adapter implementing {@link StaffAuditLogPort}. */
@Component
@RequiredArgsConstructor
class JpaStaffAuditLogAdapter implements StaffAuditLogPort {

  private final SpringDataStaffAuditLogRepository repo;

  @Override
  public void save(StaffAuditLog auditLog) {
    repo.save(auditLog);
  }
}
