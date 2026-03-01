package com.chamrong.iecommerce.invoice.domain.ports;

/**
 * Output port: asymmetric digital signing and verification of invoice payloads.
 *
 * <p><b>Algorithm</b>: Ed25519 (recommended). Implementations may also support RSA-PSS via
 * configuration.
 *
 * <p><b>Key rotation</b>: multiple key pairs may be configured, each identified by a unique {@code
 * keyId}. Signing always uses the active key; verification uses the key identified by the stored
 * {@code keyId}.
 *
 * <p><b>Security</b>: implementations must NEVER log key material or raw signature bytes.
 */
public interface DigitalSignaturePort {

  /**
   * Signs the provided payload bytes with the currently active private key.
   *
   * @param payload canonical byte representation of the invoice to sign
   * @return the signing result containing the base64 signature and key metadata
   */
  SignResult sign(byte[] payload);

  /**
   * Verifies a signature against the stored payload using the public key identified by {@code
   * keyId}.
   *
   * @param payload the same canonical byte representation used during signing
   * @param signatureBase64 base64-encoded signature value from storage
   * @param keyId key rotation identifier — used to select the correct public key
   * @return true if the signature is valid
   */
  boolean verify(byte[] payload, String signatureBase64, String keyId);

  /**
   * Result of a signing operation.
   *
   * @param signatureBase64 base64-encoded signature
   * @param keyId key rotation ID used for signing
   * @param algorithm algorithm name (e.g., "Ed25519")
   */
  record SignResult(String signatureBase64, String keyId, String algorithm) {}
}
