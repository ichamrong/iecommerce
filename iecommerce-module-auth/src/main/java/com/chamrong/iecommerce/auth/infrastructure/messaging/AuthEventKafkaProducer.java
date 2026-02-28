package com.chamrong.iecommerce.auth.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Publishes auth domain events to Kafka.
 *
 * <h3>Topics</h3>
 *
 * <ul>
 *   <li>{@link KafkaTopics#AUTH_EVENTS} — general domain events (login, register, etc.)
 *   <li>{@link KafkaTopics#AUTH_SECURITY_ALERTS} — security incidents (brute force, lockouts)
 * </ul>
 *
 * <h3>Conditional activation</h3>
 *
 * <p>This bean is only created when {@code auth.kafka.enabled=true} in the application properties.
 * This allows running the module without Kafka in development environments.
 *
 * <h3>Failure handling</h3>
 *
 * <p>Kafka publish failures are logged as errors but do not propagate — a missed analytics event
 * must never fail a user login. For guaranteed delivery, consider wiring this producer to the
 * outbox pattern in a future iteration.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class AuthEventKafkaProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  /**
   * Publishes a domain event to the main auth events topic.
   *
   * @param eventType short descriptor (e.g. {@code "UserLoggedIn"})
   * @param event the event payload — must be Jackson-serializable
   * @param tenantId used as the Kafka partition key for ordering per tenant
   */
  public void publishEvent(
      @NonNull final String eventType,
      @NonNull final Object event,
      @NonNull final String tenantId) {
    publish(KafkaTopics.AUTH_EVENTS, eventType, event, tenantId);
  }

  /**
   * Publishes a security alert to the dedicated security alerts topic.
   *
   * @param alertType short descriptor (e.g. {@code "BruteForceDetected"})
   * @param alert the alert payload — must be Jackson-serializable
   * @param tenantId used as the Kafka partition key
   */
  public void publishSecurityAlert(
      @NonNull final String alertType,
      @NonNull final Object alert,
      @NonNull final String tenantId) {
    publish(KafkaTopics.AUTH_SECURITY_ALERTS, alertType, alert, tenantId);
  }

  private void publish(
      final String topic, final String eventType, final Object payload, final String partitionKey) {
    try {
      final String json = objectMapper.writeValueAsString(payload);
      kafkaTemplate
          .send(topic, partitionKey, json)
          .whenComplete(
              (result, ex) -> {
                if (ex != null) {
                  log.error(
                      "Failed to publish eventType={} to topic={}: {}",
                      eventType,
                      topic,
                      ex.getMessage(),
                      ex);
                } else {
                  log.debug(
                      "Published eventType={} to topic={} partition={} offset={}",
                      eventType,
                      topic,
                      result.getRecordMetadata().partition(),
                      result.getRecordMetadata().offset());
                }
              });
    } catch (JsonProcessingException e) {
      log.error("Cannot serialize event eventType={}: {}", eventType, e.getMessage(), e);
    }
  }
}
