package com.chamrong.iecommerce.invoice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

/**
 * Stores the cryptographic signature of an {@link Invoice} computed at issuance time.
 *
 * <p>One record per invoice ({@code UNIQUE invoice_id}). Immutable once written.
 *
 * <p>Security: signature metadata is stored but raw keys are never persisted here.
 */
@Entity
@Table(name = "invoice_signature")
public class InvoiceSignature {

  /** Required by JPA — not for application use. */
  protected InvoiceSignature() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "invoice_id", nullable = false, unique = true, updatable = false)
  private Long invoiceId;

  /**
   * SHA-256 hex digest of the canonical (deterministic JSON) representation of the invoice at
   * issuance time.
   */
  @Column(name = "content_hash", nullable = false, updatable = false, length = 64)
  private String contentHash;

  /** Signing algorithm identifier — e.g., {@code "Ed25519"}. */
  @Column(name = "signature_algorithm", nullable = false, updatable = false, length = 50)
  private String signatureAlgorithm;

  /** Base64-encoded signature bytes produced by the private key identified by {@link #keyId}. */
  @Column(name = "signature_value", nullable = false, updatable = false, columnDefinition = "TEXT")
  private String signatureValue;

  /**
   * Identifier for the key pair used to produce this signature. Required for key rotation —
   * multiple public keys may be retained in config, identified by their {@code keyId}.
   */
  @Column(name = "key_id", nullable = false, updatable = false, length = 100)
  private String keyId;

  @Column(name = "signed_at", nullable = false, updatable = false)
  private Instant signedAt;

  /**
   * Factory method — creates an immutable signature record.
   *
   * @param invoiceId invoice this signature belongs to
   * @param contentHash SHA-256 hex of canonical invoice payload
   * @param signatureAlgorithm e.g. "Ed25519"
   * @param signatureValue base64-encoded signature
   * @param keyId key rotation identifier
   * @param signedAt moment of signing
   */
  public static InvoiceSignature create(
      Long invoiceId,
      String contentHash,
      String signatureAlgorithm,
      String signatureValue,
      String keyId,
      Instant signedAt) {
    Objects.requireNonNull(invoiceId, "invoiceId must not be null");
    Objects.requireNonNull(contentHash, "contentHash must not be null");
    Objects.requireNonNull(signatureAlgorithm, "signatureAlgorithm must not be null");
    Objects.requireNonNull(signatureValue, "signatureValue must not be null");
    Objects.requireNonNull(keyId, "keyId must not be null");
    Objects.requireNonNull(signedAt, "signedAt must not be null");

    InvoiceSignature sig = new InvoiceSignature();
    sig.invoiceId = invoiceId;
    sig.contentHash = contentHash;
    sig.signatureAlgorithm = signatureAlgorithm;
    sig.signatureValue = signatureValue;
    sig.keyId = keyId;
    sig.signedAt = signedAt;
    return sig;
  }

  // ── Accessors ──────────────────────────────────────────────────────────────

  public Long getId() {
    return id;
  }

  public Long getInvoiceId() {
    return invoiceId;
  }

  public String getContentHash() {
    return contentHash;
  }

  public String getSignatureAlgorithm() {
    return signatureAlgorithm;
  }

  public String getSignatureValue() {
    return signatureValue;
  }

  public String getKeyId() {
    return keyId;
  }

  public Instant getSignedAt() {
    return signedAt;
  }
}
