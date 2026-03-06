package com.chamrong.iecommerce.invoice.infrastructure.security;

import com.chamrong.iecommerce.invoice.domain.exception.InvoiceSignatureException;
import com.chamrong.iecommerce.invoice.domain.ports.DigitalSignaturePort;
import java.security.Security;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Ed25519 digital signature adapter (BouncyCastle).
 *
 * <h2>Key Rotation</h2>
 *
 * Configure multiple key pairs via:
 *
 * <pre>
 * invoice.signing.active-key-id=key-2026-01
 * invoice.signing.keys[0].key-id=key-2026-01
 * invoice.signing.keys[0].private-key-base64=${INVOICE_SIGNING_PRIVATE_KEY_BASE64}
 * invoice.signing.keys[0].public-key-base64=${INVOICE_SIGNING_PUBLIC_KEY_BASE64}
 * invoice.signing.keys[1].key-id=key-2025-01
 * invoice.signing.keys[1].public-key-base64=${INVOICE_OLD_PUBLIC_KEY_BASE64}
 * </pre>
 *
 * <p><b>Security</b>: this class NEVER logs key material or raw signature bytes. Keys are held in
 * memory in raw byte form (not Base64) after startup.
 */
@Slf4j
@Component
@EnableConfigurationProperties(Ed25519DigitalSignatureAdapter.SigningProperties.class)
public class Ed25519DigitalSignatureAdapter implements DigitalSignaturePort {

  private static final String ALGORITHM = "Ed25519";

  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  private final SigningProperties props;

  /** Pre-decoded public keys: keyId → raw bytes. */
  private final Map<String, byte[]> publicKeyCache;

  public Ed25519DigitalSignatureAdapter(SigningProperties props) {
    this.props = props;
    this.publicKeyCache =
        props.getKeys().stream()
            .filter(k -> k.getPublicKeyBase64() != null)
            .collect(
                Collectors.toMap(
                    SigningProperties.KeyConfig::getKeyId,
                    k -> Base64.getDecoder().decode(k.getPublicKeyBase64())));
    log.info(
        "Ed25519SignatureAdapter initialized with {} key(s), activeKeyId={}",
        publicKeyCache.size(),
        props.getActiveKeyId());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Signs using the active private key identified by {@code activeKeyId}. The private key is
   * loaded from config and decoded once here.
   */
  @Override
  public SignResult sign(byte[] payload) {
    String activeKeyId = props.getActiveKeyId();
    SigningProperties.KeyConfig activeKey =
        props.getKeys().stream()
            .filter(k -> activeKeyId.equals(k.getKeyId()))
            .findFirst()
            .orElseThrow(
                () ->
                    new InvoiceSignatureException("Active signing key not found: " + activeKeyId));

    if (activeKey.getPrivateKeyBase64() == null) {
      throw new InvoiceSignatureException("No private key configured for keyId=" + activeKeyId);
    }

    try {
      byte[] privateBytes = Base64.getDecoder().decode(activeKey.getPrivateKeyBase64());
      Ed25519PrivateKeyParameters privateKey = new Ed25519PrivateKeyParameters(privateBytes, 0);

      Ed25519Signer signer = new Ed25519Signer();
      signer.init(true, privateKey);
      signer.update(payload, 0, payload.length);
      byte[] signatureBytes = signer.generateSignature();

      // NEVER log signatureBytes or privateBytes
      log.debug("Invoice payload signed with keyId={}", activeKeyId);

      return new SignResult(
          Base64.getEncoder().encodeToString(signatureBytes), activeKeyId, ALGORITHM);
    } catch (Exception e) {
      throw new InvoiceSignatureException("Signing failed for keyId=" + activeKeyId, e);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Looks up the public key by {@code keyId} to support verification of historically-signed
   * invoices even after key rotation.
   */
  @Override
  public boolean verify(byte[] payload, String signatureBase64, String keyId) {
    byte[] pubKeyBytes = publicKeyCache.get(keyId);
    if (pubKeyBytes == null) {
      log.warn("No public key found for keyId={}; verification returns false", keyId);
      return false;
    }

    try {
      byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
      Ed25519PublicKeyParameters publicKey = new Ed25519PublicKeyParameters(pubKeyBytes, 0);

      Ed25519Signer verifier = new Ed25519Signer();
      verifier.init(false, publicKey);
      verifier.update(payload, 0, payload.length);
      return verifier.verifySignature(signatureBytes);
    } catch (Exception e) {
      log.warn("Signature verification threw exception for keyId={}; returning false", keyId);
      return false;
    }
  }

  // ── Configuration properties ──────────────────────────────────────────────

  @ConfigurationProperties(prefix = "invoice.signing")
  public static class SigningProperties {

    /** The keyId of the key pair to use for new signatures. */
    private String activeKeyId = "default";

    /** All configured key pairs. Historical public keys must be retained for verification. */
    private List<KeyConfig> keys = List.of();

    public String getActiveKeyId() {
      return activeKeyId;
    }

    public void setActiveKeyId(String activeKeyId) {
      this.activeKeyId = activeKeyId;
    }

    public List<KeyConfig> getKeys() {
      return keys;
    }

    public void setKeys(List<KeyConfig> keys) {
      this.keys = keys;
    }

    public static class KeyConfig {
      private String keyId;

      /** Base64-encoded 32-byte Ed25519 private key seed. Present only for active signing key. */
      private String privateKeyBase64;

      /**
       * Base64-encoded 32-byte Ed25519 public key. Must be present for all keys (signing + old).
       */
      private String publicKeyBase64;

      public String getKeyId() {
        return keyId;
      }

      public void setKeyId(String keyId) {
        this.keyId = keyId;
      }

      public String getPrivateKeyBase64() {
        return privateKeyBase64;
      }

      public void setPrivateKeyBase64(String privateKeyBase64) {
        this.privateKeyBase64 = privateKeyBase64;
      }

      public String getPublicKeyBase64() {
        return publicKeyBase64;
      }

      public void setPublicKeyBase64(String publicKeyBase64) {
        this.publicKeyBase64 = publicKeyBase64;
      }
    }
  }
}
