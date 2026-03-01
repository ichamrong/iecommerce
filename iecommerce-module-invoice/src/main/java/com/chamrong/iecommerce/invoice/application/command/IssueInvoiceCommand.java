package com.chamrong.iecommerce.invoice.application.command;

/**
 * Command: issue a DRAFT invoice — assigns number, signs, and locks content.
 *
 * @param tenantId owning tenant (from JWT)
 * @param actorId authenticated user who triggered issuance
 * @param invoiceId the draft invoice to issue
 * @param sellerSnapshot optional fresh JSON of seller info; if null, keeps existing snapshot
 * @param buyerSnapshot optional fresh JSON of buyer info; if null, keeps existing snapshot
 */
public record IssueInvoiceCommand(
    String tenantId, String actorId, Long invoiceId, String sellerSnapshot, String buyerSnapshot) {}
