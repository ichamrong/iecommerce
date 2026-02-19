package com.chamrong.iecommerce.audit.domain;

import java.util.List;
import java.util.Optional;

public interface AuditRepository {
  AuditEvent save(AuditEvent event);

  Optional<AuditEvent> findById(Long id);

  List<AuditEvent> findByUserId(String userId);
}
