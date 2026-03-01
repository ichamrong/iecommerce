package com.chamrong.iecommerce.invoice.domain.ports;

/**
 * Output port: publish invoice domain events via the transactional outbox.
 *
 * <p>All events are persisted in the same DB transaction as the business operation to guarantee
 * at-least-once delivery (outbox pattern).
 */
public interface InvoiceOutboxPort {

  /**
   * Enqueues an {@code INVOICE_ISSUED} event for the given invoice.
   *
   * @param tenantId owning tenant
   * @param invoiceId the issued invoice ID
   * @param invoiceNumber the assigned invoice number
   * @param orderId optional linked order ID
   */
  void publishIssued(String tenantId, Long invoiceId, String invoiceNumber, Long orderId);

  /**
   * Enqueues an {@code INVOICE_VOIDED} event.
   *
   * @param tenantId owning tenant
   * @param invoiceId the voided invoice ID
   * @param reason void reason for downstream consumers
   */
  void publishVoided(String tenantId, Long invoiceId, String reason);

  /**
   * Enqueues an {@code INVOICE_PAID} event.
   *
   * @param tenantId owning tenant
   * @param invoiceId the paid invoice ID
   * @param paymentReference payment provider reference
   */
  void publishPaid(String tenantId, Long invoiceId, String paymentReference);
}
