package com.chamrong.iecommerce.audit.application;

import com.chamrong.iecommerce.audit.application.dto.AuditQuery;
import com.chamrong.iecommerce.audit.application.dto.AuditResponse;
import com.chamrong.iecommerce.audit.domain.AuditEvent;
import com.chamrong.iecommerce.audit.domain.AuditRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    try {
      var requestAttributes = RequestContextHolder.getRequestAttributes();
      if (requestAttributes instanceof ServletRequestAttributes servletAttributes) {
        var request = servletAttributes.getRequest();
        var ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
          ip = request.getRemoteAddr();
        } else {
          ip = ip.split(",")[0].trim();
        }
        event.setIpAddress(ip != null && ip.length() > 45 ? ip.substring(0, 45) : ip);

        var userAgent = request.getHeader("User-Agent");
        event.setUserAgent(
            userAgent != null && userAgent.length() > 500
                ? userAgent.substring(0, 500)
                : userAgent);
      }
    } catch (Exception e) {
      // Ignore context errors during async event handling
    }

    auditRepository.save(event);
  }

  @Transactional(readOnly = true)
  public Optional<AuditResponse> findById(Long id) {
    return auditRepository.findById(id).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public Page<AuditResponse> findAll(Pageable pageable) {
    return auditRepository.findAll(pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public Page<AuditResponse> findByUserId(String userId, Pageable pageable) {
    return auditRepository.findByUserId(userId, pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public Page<AuditResponse> findByQuery(AuditQuery query, Pageable pageable) {
    return auditRepository.findByQuery(query, pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public List<String> getUniqueActions() {
    return auditRepository.findUniqueActions();
  }

  @Transactional(readOnly = true)
  public List<String> getUniqueResourceTypes() {
    return auditRepository.findUniqueResourceTypes();
  }

  private AuditResponse toResponse(AuditEvent event) {
    return new AuditResponse(
        event.getId(),
        event.getUserId(),
        event.getAction(),
        event.getResourceType(),
        event.getResourceId(),
        event.getMetadata(),
        event.getIpAddress(),
        event.getUserAgent(),
        event.getTimestamp());
  }
}
