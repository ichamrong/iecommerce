package com.chamrong.iecommerce.promotion.application.dto;

/** Response for redemption operations. */
public record RedemptionResponse(String redemptionKey, String status, Double discountAmount) {}
