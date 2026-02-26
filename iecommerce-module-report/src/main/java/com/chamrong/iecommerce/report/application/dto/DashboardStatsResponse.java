package com.chamrong.iecommerce.report.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DashboardStatsResponse(
    long totalOrders,
    BigDecimal totalRevenue,
    String currency,
    long pendingOrders,
    Instant generatedAt) {}
