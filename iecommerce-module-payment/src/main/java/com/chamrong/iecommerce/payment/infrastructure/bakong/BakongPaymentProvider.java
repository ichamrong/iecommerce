package com.chamrong.iecommerce.payment.infrastructure.bakong;

import com.chamrong.iecommerce.payment.application.spi.PaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BakongPaymentProvider implements PaymentProvider {

  private final BakongService bakongService;

  @Override
  public String initiatePayment(String orderId, java.math.BigDecimal amount, String currency) {
    return bakongService.generateKhqr(orderId, amount);
  }

  @Override
  public boolean supports(String method) {
    return "BAKONG".equalsIgnoreCase(method) || "KHQR".equalsIgnoreCase(method);
  }
}
