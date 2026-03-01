package com.chamrong.iecommerce.sale.domain.service;

import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.JpaAuditLogRepository;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.SaleAuditLogEntity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

  private final JpaAuditLogRepository repository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void log(
      String tenantId,
      String actorId,
      String action,
      String entityName,
      String entityId,
      String correlationId,
      Object beforeState,
      Object afterState) {

    String beforeHash = beforeState != null ? hash(beforeState.toString()) : null;
    String afterHash = afterState != null ? hash(afterState.toString()) : null;

    String prevHash =
        repository
            .findLatestByTenantId(tenantId)
            .map(SaleAuditLogEntity::getRecordHash)
            .orElse(null);

    String recordHash =
        calculateRecordHash(
            tenantId, actorId, action, entityId, correlationId, beforeHash, afterHash, prevHash);

    SaleAuditLogEntity entity =
        new SaleAuditLogEntity(
            tenantId,
            actorId,
            action,
            entityName,
            entityId,
            correlationId,
            beforeHash,
            afterHash,
            prevHash,
            recordHash);

    repository.save(entity);
  }

  private String calculateRecordHash(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
      if (part != null) sb.append(part);
    }
    return hash(sb.toString());
  }

  private String hash(String text) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 algorithm not found", e);
    }
  }
}
