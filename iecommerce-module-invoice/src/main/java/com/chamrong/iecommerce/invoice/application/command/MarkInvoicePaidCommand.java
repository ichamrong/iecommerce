package com.chamrong.iecommerce.invoice.application.command;

/**
 * Command: mark an issued/overdue invoice as paid.
 *
 * @param tenantId owning tenant (from JWT)
 * @param actorId authenticated user
 * @param invoiceId invoice to mark paid
 * @param paymentReference external payment provider transaction reference
 */
public record MarkInvoicePaidCommand(
    String tenantId, String actorId, Long invoiceId, String paymentReference) {}
