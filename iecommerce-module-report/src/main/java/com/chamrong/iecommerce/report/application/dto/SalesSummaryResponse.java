package com.chamrong.iecommerce.report.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SalesSummaryResponse(
    Instant periodStart,
    Instant periodEnd,
    String periodLabel,
    long orderCount,
    BigDecimal revenue,
    String currency) {}
