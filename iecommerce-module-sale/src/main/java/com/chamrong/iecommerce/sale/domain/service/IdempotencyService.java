package com.chamrong.iecommerce.sale.domain.service;

import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.JpaIdempotencyRepository;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.SaleIdempotencyEntity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

  private final JpaIdempotencyRepository repository;

  @Transactional(readOnly = true)
  public Optional<String> getResponse(
      String tenantId, String idempotencyKey, String endpointName, String requestPayload) {
    String requestHash = hash(requestPayload);
    return repository
        .findByTenantIdAndIdempotencyKeyAndEndpointName(tenantId, idempotencyKey, endpointName)
        .filter(entity -> entity.getRequestHash().equals(requestHash))
        .map(SaleIdempotencyEntity::getResponseSnapshot);
  }

  @Transactional
  public void saveResponse(
      String tenantId,
      String idempotencyKey,
      String endpointName,
      String requestPayload,
      String responseSnapshot) {
    String requestHash = hash(requestPayload);
    SaleIdempotencyEntity entity =
        new SaleIdempotencyEntity(
            tenantId, idempotencyKey, endpointName, requestHash, responseSnapshot);
    repository.save(entity);
  }

  /** Orchestrates idempotent execution. */
  @Transactional
  public Object execute(
      String tenantId,
      String idempotencyKey,
      String endpointName,
      String requestPayload,
      Supplier<Object> supplier) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      return supplier.get();
    }

    Optional<String> existing = getResponse(tenantId, idempotencyKey, endpointName, requestPayload);
    if (existing.isPresent()) {
      log.info("Idempotent hit for {} in tenant {}", endpointName, tenantId);
      // In a real app, deserialize snapshot. For now, we return null or handle via caller.
      return null;
    }

    Object result = supplier.get();
    saveResponse(
        tenantId,
        idempotencyKey,
        endpointName,
        requestPayload,
        result != null ? result.toString() : "SUCCESS");
    return result;
  }

  private String hash(String text) {
    if (text == null) text = "";
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
