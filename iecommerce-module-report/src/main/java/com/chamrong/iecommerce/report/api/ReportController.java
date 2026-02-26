package com.chamrong.iecommerce.report.api;

import com.chamrong.iecommerce.report.application.SalesAnalyticsService;
import com.chamrong.iecommerce.report.application.dto.DashboardStatsResponse;
import com.chamrong.iecommerce.report.application.dto.SalesSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sales analytics and reporting endpoints.
 *
 * <p>Base path: {@code /api/v1/reports}
 */
@Tag(name = "Reports", description = "Sales analytics and business intelligence")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('reports:read')")
public class ReportController {

  private final SalesAnalyticsService analyticsService;

  @Operation(
      summary = "Dashboard stats",
      description = "Returns high-level KPIs: total orders, revenue, and pending orders.")
  @GetMapping("/dashboard")
  public DashboardStatsResponse getDashboard() {
    return analyticsService.getBasicDashboardStats();
  }

  @Operation(
      summary = "Daily sales summary",
      description = "Returns per-day order counts and revenue within the given date range.")
  @GetMapping("/sales/daily")
  public List<SalesSummaryResponse> getDailySales(
      @RequestParam Instant from, @RequestParam Instant to) {
    return analyticsService.getDailySales(from, to);
  }

  @Operation(
      summary = "Monthly sales summary",
      description = "Returns per-month order counts and revenue for the given calendar year.")
  @GetMapping("/sales/monthly")
  public List<SalesSummaryResponse> getMonthlySales(
      @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") int year) {
    return analyticsService.getMonthlySales(year);
  }

  @Operation(
      summary = "Top-selling products",
      description = "Returns the IDs of the top N products by revenue in the given date range.")
  @GetMapping("/products/top")
  public List<Long> getTopProducts(
      @RequestParam Instant from,
      @RequestParam Instant to,
      @RequestParam(defaultValue = "10") int limit) {
    return analyticsService.getTopProductIds(from, to, limit);
  }
}
