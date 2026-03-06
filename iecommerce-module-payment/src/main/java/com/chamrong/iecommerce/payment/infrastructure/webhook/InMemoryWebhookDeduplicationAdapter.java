package com.chamrong.iecommerce.payment.infrastructure.webhook;

import com.chamrong.iecommerce.payment.domain.webhook.WebhookDeduplicationPort;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Simple in-memory implementation of {@link WebhookDeduplicationPort} for local development.
 *
 * <p>For production deployments this should be replaced with a Redis- or database-backed
 * implementation to ensure idempotency across instances.
 */
@Component
public class InMemoryWebhookDeduplicationAdapter implements WebhookDeduplicationPort {

  private final Set<String> processed = ConcurrentHashMap.newKeySet();

  @Override
  public boolean markAsProcessed(String providerEventId) {
    return processed.add(providerEventId);
  }

  @Override
  public boolean isAlreadyProcessed(String providerEventId) {
    return processed.contains(providerEventId);
  }
}
