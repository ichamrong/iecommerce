package com.chamrong.iecommerce.report.domain.service;

import com.chamrong.iecommerce.report.domain.model.ReportResult;
import com.chamrong.iecommerce.report.domain.model.ReportType;
import java.time.ZoneId;
import java.util.Map;

/**
 * Strategy for generating a specific family of reports.
 *
 * <p>Each implementation is responsible for one logical group (e.g. sales, payment, promotion) and
 * may support multiple {@link ReportType} values within that group.
 */
public interface ReportStrategy {

  /** Returns true if this strategy can handle the given report type. */
  boolean supports(ReportType type);

  /**
   * Generates a {@link ReportResult} for the given tenant and filters.
   *
   * @param tenantId tenant scope
   * @param type logical report type
   * @param filters strongly-typed but opaque filter map (already validated)
   * @param timezone IANA timezone for date bucketing / formatting
   */
  ReportResult generate(
      String tenantId, ReportType type, Map<String, Object> filters, ZoneId timezone);
}
