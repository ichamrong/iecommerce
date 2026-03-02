package com.chamrong.iecommerce.sale.application;

import com.chamrong.iecommerce.sale.domain.ports.IdempotencyPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application-level helper that orchestrates idempotent execution for sale use cases.
 *
 * <p>Encapsulates hashing and snapshot storage behind the {@link IdempotencyPort}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotentExecutor {

  private final IdempotencyPort idempotencyPort;

  @Transactional
  public <T> T execute(
      String tenantId,
      String idempotencyKey,
      String endpointName,
      String requestPayload,
      Supplier<T> supplier) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      return supplier.get();
    }

    String requestHash = hash(requestPayload);
    Optional<String> existing =
        idempotencyPort.findSnapshot(tenantId, idempotencyKey, endpointName, requestHash);
    if (existing.isPresent()) {
      log.info("Idempotent hit for {} in tenant {}", endpointName, tenantId);
      // For now we only support success/no-op replay; callers treat this as already processed.
      return null;
    }

    T result = supplier.get();
    String snapshot = result != null ? result.toString() : "SUCCESS";
    idempotencyPort.saveSnapshot(tenantId, idempotencyKey, endpointName, requestHash, snapshot);
    return result;
  }

  private String hash(String text) {
    if (text == null) {
      text = "";
    }
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
