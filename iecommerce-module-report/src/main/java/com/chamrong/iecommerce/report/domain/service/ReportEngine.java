package com.chamrong.iecommerce.report.domain.service;

import com.chamrong.iecommerce.report.domain.exception.ReportDomainException;
import com.chamrong.iecommerce.report.domain.model.ReportResult;
import com.chamrong.iecommerce.report.domain.model.ReportType;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * Central entry point for executing reports using pluggable {@link ReportStrategy} implementations.
 */
public final class ReportEngine {

  private final List<ReportStrategy> strategies;

  public ReportEngine(List<ReportStrategy> strategies) {
    this.strategies = List.copyOf(strategies);
  }

  public ReportResult run(
      String tenantId, ReportType type, Map<String, Object> filters, ZoneId timezone) {
    for (ReportStrategy strategy : strategies) {
      if (strategy.supports(type)) {
        return strategy.generate(tenantId, type, filters, timezone);
      }
    }
    throw new ReportDomainException("No report strategy registered for type " + type);
  }
}
