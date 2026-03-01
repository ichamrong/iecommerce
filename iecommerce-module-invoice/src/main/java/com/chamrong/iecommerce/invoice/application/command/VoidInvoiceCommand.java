package com.chamrong.iecommerce.invoice.application.command;

/**
 * Command: void an issued (or overdue) invoice.
 *
 * @param tenantId owning tenant (from JWT)
 * @param actorId authenticated user performing the void
 * @param invoiceId invoice to void
 * @param reason mandatory non-blank void reason
 */
public record VoidInvoiceCommand(String tenantId, String actorId, Long invoiceId, String reason) {}
