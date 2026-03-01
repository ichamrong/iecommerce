package com.chamrong.iecommerce.payment.application;

import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.WebhookVerificationPort;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookVerificationService {

  private final List<WebhookVerificationPort> verifiers;

  public WebhookVerificationPort.VerificationResult verify(
      ProviderType provider, String payload, Map<String, String> headers) {
    return verifiers.stream()
        .map(v -> v.verify(provider, payload, headers))
        .filter(WebhookVerificationPort.VerificationResult::isValid)
        .findFirst()
        .orElse(
            new WebhookVerificationPort.VerificationResult(
                false, null, null, null, payload, "No valid verifier found"));
  }
}
