package com.chamrong.iecommerce.payment.domain;

public enum ProviderType {
  STRIPE,
  PAYPAL,
  ABA,
  BAKONG,
  MANUAL // For POS or manual bank transfers
}
