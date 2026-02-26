package com.chamrong.iecommerce.payment.application.dto;

import java.math.BigDecimal;

public record PaymentRequest(Long orderId, BigDecimal amount, String currency, String method) {}
