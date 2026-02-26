package com.chamrong.iecommerce.subscription.application.dto;

import java.time.Instant;

public record TenantSubscriptionResponse(
    Long id,
    String tenantId,
    String planCode,
    String planName,
    String status,
    Instant startDate,
    Instant endDate,
    Instant nextBillingDate,
    boolean autoRenew) {}
