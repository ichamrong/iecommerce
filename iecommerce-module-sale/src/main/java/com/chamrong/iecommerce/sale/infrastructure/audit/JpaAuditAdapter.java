package com.chamrong.iecommerce.sale.infrastructure.audit;

import com.chamrong.iecommerce.sale.domain.ports.AuditPort;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.JpaAuditLogRepository;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.SaleAuditLogEntity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Infrastructure adapter that persists audit records using JPA.
 *
 * <p>Provides tamper-evident chaining via SHA-256 hashes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JpaAuditAdapter implements AuditPort {

  private final JpaAuditLogRepository repository;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void log(
      String tenantId,
      String actorId,
      String action,
      String entityName,
      String entityId,
      String correlationId,
      String beforeState,
      String afterState) {

    String beforeHash = beforeState != null ? hash(beforeState) : null;
    String afterHash = afterState != null ? hash(afterState) : null;

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
      if (part != null) {
        sb.append(part);
      }
    }
    return hash(sb.toString());
  }

  private String hash(String text) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not found", e);
    }
  }
}
