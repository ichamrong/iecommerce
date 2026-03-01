package com.chamrong.iecommerce.invoice.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.invoice.domain.ports.DigitalSignaturePort.SignResult;
import java.security.Security;
import java.util.Base64;
import java.util.List;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Ed25519DigitalSignatureAdapter}: - sign + verify round-trip - tampered
 * payload fails verification - missing key ID returns false (not exception)
 */
class Ed25519SignatureAdapterTest {

  private static Ed25519DigitalSignatureAdapter adapter;
  private static final String KEY_ID = "test-key-2026";

  @BeforeAll
  static void setup() {
    Security.addProvider(new BouncyCastleProvider());

    // Generate a fresh Ed25519 key pair for testing
    Ed25519KeyPairGenerator gen = new Ed25519KeyPairGenerator();
    gen.init(new Ed25519KeyGenerationParameters(new java.security.SecureRandom()));
    AsymmetricCipherKeyPair pair = gen.generateKeyPair();

    String privateBase64 =
        Base64.getEncoder()
            .encodeToString(((Ed25519PrivateKeyParameters) pair.getPrivate()).getEncoded());
    String publicBase64 =
        Base64.getEncoder()
            .encodeToString(((Ed25519PublicKeyParameters) pair.getPublic()).getEncoded());

    Ed25519DigitalSignatureAdapter.SigningProperties props =
        new Ed25519DigitalSignatureAdapter.SigningProperties();
    props.setActiveKeyId(KEY_ID);

    Ed25519DigitalSignatureAdapter.SigningProperties.KeyConfig keyConfig =
        new Ed25519DigitalSignatureAdapter.SigningProperties.KeyConfig();
    keyConfig.setKeyId(KEY_ID);
    keyConfig.setPrivateKeyBase64(privateBase64);
    keyConfig.setPublicKeyBase64(publicBase64);
    props.setKeys(List.of(keyConfig));

    adapter = new Ed25519DigitalSignatureAdapter(props);
  }

  @Test
  void sign_thenVerify_returnsTrue() {
    byte[] payload = "invoice-canonical-payload".getBytes();
    SignResult result = adapter.sign(payload);

    assertThat(result.keyId()).isEqualTo(KEY_ID);
    assertThat(result.algorithm()).isEqualTo("Ed25519");
    assertThat(result.signatureBase64()).isNotBlank();

    boolean valid = adapter.verify(payload, result.signatureBase64(), KEY_ID);
    assertThat(valid).isTrue();
  }

  @Test
  void tamperedPayload_failsVerification() {
    byte[] original = "invoice-payload".getBytes();
    SignResult result = adapter.sign(original);

    byte[] tampered = "invoice-TAMPERED".getBytes();
    boolean valid = adapter.verify(tampered, result.signatureBase64(), KEY_ID);
    assertThat(valid).isFalse();
  }

  @Test
  void unknownKeyId_returnsFalse() {
    byte[] payload = "payload".getBytes();
    SignResult result = adapter.sign(payload);

    boolean valid = adapter.verify(payload, result.signatureBase64(), "unknown-key");
    assertThat(valid).isFalse();
  }
}
