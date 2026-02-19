package com.chamrong.iecommerce.order.domain;

public enum OrderState {
  AddingItems,
  ArrangingPayment,
  PaymentAuthorized,
  PaymentSettled,
  Shipped,
  Delivered,
  Cancelled
}
