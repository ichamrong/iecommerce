package com.chamrong.iecommerce.audit.infrastructure;

import com.chamrong.iecommerce.audit.domain.AuditEvent;
import com.chamrong.iecommerce.audit.domain.ports.AuditRepositoryPort;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA adapter implementing {@link AuditRepositoryPort}. Keyset list via custom impl.
 */
@Repository
public interface JpaAuditRepository
    extends JpaRepository<AuditEvent, Long>,
        JpaSpecificationExecutor<AuditEvent>,
        AuditRepositoryPort,
        JpaAuditRepositoryCustom {

  @Override
  @Query("SELECT DISTINCT e.action FROM AuditEvent e ORDER BY e.action")
  List<String> findUniqueActions();

  @Override
  @Query("SELECT DISTINCT e.resourceType FROM AuditEvent e ORDER BY e.resourceType")
  List<String> findUniqueResourceTypes();
}
