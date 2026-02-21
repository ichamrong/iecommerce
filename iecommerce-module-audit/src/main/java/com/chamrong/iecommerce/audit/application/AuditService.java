package com.chamrong.iecommerce.audit.application;

import com.chamrong.iecommerce.audit.domain.AuditEvent;
import com.chamrong.iecommerce.audit.domain.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

  private final AuditRepository auditRepository;

  @Transactional
  public void log(
      String userId, String action, String resourceType, String resourceId, String metadata) {
    var event = new AuditEvent();
    event.setUserId(userId);
    event.setAction(action);
    event.setResourceType(resourceType);
    event.setResourceId(resourceId);
    event.setMetadata(metadata);

    auditRepository.save(event);
  }
}
