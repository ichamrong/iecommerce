package com.chamrong.iecommerce.audit.infrastructure;

import com.chamrong.iecommerce.audit.domain.AuditEvent;
import com.chamrong.iecommerce.audit.domain.AuditRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link AuditRepository} port. */
@Repository
public interface JpaAuditRepository extends JpaRepository<AuditEvent, Long>, AuditRepository {
  @Override
  List<AuditEvent> findByUserId(String userId);
}
