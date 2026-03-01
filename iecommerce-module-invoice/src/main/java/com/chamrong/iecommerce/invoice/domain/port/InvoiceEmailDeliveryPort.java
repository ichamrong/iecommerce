package com.chamrong.iecommerce.invoice.domain.port;

import com.chamrong.iecommerce.invoice.infrastructure.email.InvoiceEmailDelivery;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Output port: idempotency store for invoice email delivery attempts.
 *
 * <p>Used by both the application service (to queue resends) and the email handler (to check for
 * duplicate-send prevention and record outcomes).
 *
 * <p>ASVS V14.5 — All reads/writes go through this port to ensure idempotency semantics are
 * centralised.
 */
public interface InvoiceEmailDeliveryPort {

  /** Saves (insert or update) a delivery record. */
  InvoiceEmailDelivery save(InvoiceEmailDelivery delivery);

  /** Finds by composite idempotency key {@code "{invoiceId}:{messageType}"}. */
  Optional<InvoiceEmailDelivery> findByIdempotencyKey(String idempotencyKey);

  /** Finds by invoice ID and message type. */
  Optional<InvoiceEmailDelivery> findByInvoiceIdAndMessageType(
      Long invoiceId, InvoiceEmailDelivery.MessageType messageType);

  /**
   * Returns PENDING delivery records whose {@code lastAttemptedAt} is before {@code cutoff}. Used
   * by the retry relay to re-attempt failed deliveries.
   */
  List<InvoiceEmailDelivery> findRetryable(Instant cutoff);
}
