package com.chamrong.iecommerce.subscription.application.dto;

import com.chamrong.iecommerce.common.Money;

public record SubscriptionPlanResponse(
    Long id,
    String code,
    String name,
    String description,
    Money price,
    int maxProducts,
    int maxOrdersPerMonth,
    int maxStaffProfiles,
    boolean customDomainAllowed,
    boolean active) {}
