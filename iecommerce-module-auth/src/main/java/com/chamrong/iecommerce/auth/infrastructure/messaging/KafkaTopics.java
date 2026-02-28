package com.chamrong.iecommerce.auth.infrastructure.messaging;

/**
 * Constants for Kafka topic names used by the auth module.
 *
 * <p>Topic names are final constants to prevent magic-string drift across producers and any future
 * consumers in other modules.
 */
public final class KafkaTopics {

  /** General domain events: login, register, password reset, 2FA changes. */
  public static final String AUTH_EVENTS = "auth.events";

  /**
   * High-priority security alerts: failed logins (potential brute force), rate limit hits, IDOR
   * attempts, account lockouts.
   */
  public static final String AUTH_SECURITY_ALERTS = "auth.security-alerts";

  private KafkaTopics() {
    throw new UnsupportedOperationException("Constant class");
  }
}
