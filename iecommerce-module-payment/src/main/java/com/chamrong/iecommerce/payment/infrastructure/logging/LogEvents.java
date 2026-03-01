package com.chamrong.iecommerce.payment.infrastructure.logging;

/**
 * Standard log events for the Payment Module. Aligned with SonarQube S1192 to avoid duplicated
 * string literals.
 */
public final class LogEvents {

  private LogEvents() {
    // Utility class
  }

  public static final String PAYMENT_INTENT_CREATED = "PAYMENT_INTENT_CREATED";
  public static final String PAYMENT_INTENT_STARTED = "PAYMENT_INTENT_STARTED";
  public static final String PAYMENT_INTENT_SUCCEEDED = "PAYMENT_INTENT_SUCCEEDED";
  public static final String PAYMENT_INTENT_FAILED = "PAYMENT_INTENT_FAILED";

  public static final String WEBHOOK_RECEIVED = "WEBHOOK_RECEIVED";
  public static final String WEBHOOK_VERIFIED = "WEBHOOK_VERIFIED";
  public static final String WEBHOOK_VERIFICATION_FAILED = "WEBHOOK_VERIFICATION_FAILED";
  public static final String WEBHOOK_DEDUPLICATED = "WEBHOOK_DEDUPLICATED";

  public static final String LEDGER_POSTED = "LEDGER_POSTED";
  public static final String OUTBOX_PUBLISHED = "OUTBOX_PUBLISHED";
  public static final String OUTBOX_RELAY_SUCCESS = "OUTBOX_RELAY_SUCCESS";
  public static final String OUTBOX_RELAY_FAILURE = "OUTBOX_RELAY_FAILURE";

  public static final String PROVIDER_CALL_START = "PROVIDER_CALL_START";
  public static final String PROVIDER_CALL_SUCCESS = "PROVIDER_CALL_SUCCESS";
  public static final String PROVIDER_CALL_FAILURE = "PROVIDER_CALL_FAILURE";
}
