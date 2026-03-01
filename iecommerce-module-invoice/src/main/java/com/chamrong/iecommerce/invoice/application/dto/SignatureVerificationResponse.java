package com.chamrong.iecommerce.invoice.application.dto;

import java.time.Instant;

/**
 * Response from the signature verification endpoint.
 *
 * @param invoiceId invoice that was verified
 * @param contentHash SHA-256 hex hash of the canonical invoice payload
 * @param signatureValid true if the stored signature matches the re-computed payload hash
 * @param keyId key rotation ID used when signing
 * @param signedAt when the signature was produced
 * @param reason null when valid; error reason when signatureValid=false (e.g., "HASH_MISMATCH",
 *     "SIGNATURE_INVALID", "NO_SIGNATURE")
 */
public record SignatureVerificationResponse(
    Long invoiceId,
    String contentHash,
    boolean signatureValid,
    String keyId,
    Instant signedAt,
    String reason) {}
