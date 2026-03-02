package com.chamrong.iecommerce.report.domain.model;

import java.util.List;
import java.util.Map;

/**
 * Generic container for tabular report data.
 *
 * <p>Represents a page or full set of rows as a list of column-name-to-value maps plus optional
 * summary fields.
 */
public record ReportResult(List<Map<String, Object>> rows, Map<String, Object> summary) {}
