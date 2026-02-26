package com.chamrong.iecommerce.payment.infrastructure.aba;

import com.chamrong.iecommerce.payment.application.spi.PaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AbaPaymentProvider implements PaymentProvider {

  private final ABAService abaService;

  @Override
  public String initiatePayment(String orderId, java.math.BigDecimal amount, String currency) {
    return abaService.generateHash(
        String.valueOf(System.currentTimeMillis()),
        "M_ID",
        orderId,
        amount.toString(),
        "",
        "",
        "",
        "",
        "",
        "",
        "cards",
        "aba_pay",
        "",
        "",
        "");
  }

  @Override
  public boolean supports(String method) {
    return "ABA".equalsIgnoreCase(method);
  }
}
