package com.chamrong.iecommerce.payment.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.payment.domain.ProviderType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaWebhookDedupAdapter {

  private final WebhookSpringRepository repository;

  public boolean alreadyProcessed(ProviderType provider, String eventId) {
    if (eventId != null && !eventId.isEmpty()) {
      return repository.findByProviderAndProviderEventId(provider, eventId).isPresent();
    }
    return false;
  }

  public boolean alreadyProcessedByHash(ProviderType provider, String hash) {
    if (hash == null) return false;
    return repository.findByProviderAndPayloadHash(provider, hash).isPresent();
  }

  public void record(ProviderType provider, String eventId, String type, String payload) {
    String finalEventId = eventId != null ? eventId : "GEN-" + java.util.UUID.randomUUID();
    String payloadHash =
        com.chamrong.iecommerce.payment.infrastructure.util.Sha256Util.calculateHash(payload);

    WebhookEventEntity entity =
        new WebhookEventEntity(provider, finalEventId, type, payload, payloadHash);
    repository.save(entity);
  }

  interface WebhookSpringRepository extends JpaRepository<WebhookEventEntity, Long> {
    Optional<WebhookEventEntity> findByProviderAndProviderEventId(
        ProviderType provider, String providerEventId);

    Optional<WebhookEventEntity> findByProviderAndPayloadHash(
        ProviderType provider, String payloadHash);
  }
}
