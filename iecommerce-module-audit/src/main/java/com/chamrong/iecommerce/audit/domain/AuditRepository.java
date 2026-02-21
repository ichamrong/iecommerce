package com.chamrong.iecommerce.audit.domain;

import com.chamrong.iecommerce.audit.application.dto.AuditQuery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditRepository {
  AuditEvent save(AuditEvent event);

  Optional<AuditEvent> findById(Long id);

  Page<AuditEvent> findAll(Pageable pageable);

  Page<AuditEvent> findByUserId(String userId, Pageable pageable);

  Page<AuditEvent> findByQuery(AuditQuery query, Pageable pageable);

  List<String> findUniqueActions();

  List<String> findUniqueResourceTypes();
}
