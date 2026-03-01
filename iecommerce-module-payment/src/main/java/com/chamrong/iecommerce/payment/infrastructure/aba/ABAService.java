package com.chamrong.iecommerce.payment.infrastructure.aba;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ABAService {

  private static final Logger log = LoggerFactory.getLogger(ABAService.class);
  private final ABAConfiguration config;

  /**
   * Generates the hash required by ABA Pay. Logic: Base64(HMAC-SHA512(apiKey,
   * concatenationOfFields))
   */
  public String generateHash(
      String reqTime,
      String merchantId,
      String tranId,
      String amount,
      String items,
      String shipping,
      String firstName,
      String lastName,
      String email,
      String phone,
      String type,
      String paymentOption,
      String returnUrl,
      String cancelUrl,
      String continueSuccessUrl) {
    String rawData =
        reqTime
            + merchantId
            + tranId
            + amount
            + items
            + shipping
            + firstName
            + lastName
            + email
            + phone
            + type
            + paymentOption
            + returnUrl
            + cancelUrl
            + continueSuccessUrl;
    return hmacSha512(rawData, config.getApiKey());
  }

  private String hmacSha512(String data, String key) {
    try {
      Mac sha512Hmac = Mac.getInstance("HmacSHA512");
      SecretKeySpec secretKey =
          new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
      sha512Hmac.init(secretKey);
      byte[] hash = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (Exception e) {
      log.error("Failed to generate ABA hash", e);
      throw new RuntimeException("Hash generation failed", e);
    }
  }
}
