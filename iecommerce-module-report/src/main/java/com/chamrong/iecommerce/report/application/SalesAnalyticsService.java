package com.chamrong.iecommerce.report.application;

import com.chamrong.iecommerce.catalog.CatalogApi;
import com.chamrong.iecommerce.order.OrderApi;
import com.chamrong.iecommerce.report.application.dto.DashboardStatsResponse;
import com.chamrong.iecommerce.report.application.dto.SalesSummaryResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Sales analytics and reporting service.
 *
 * <p>Aggregates data from the Order and Catalog modules via their public APIs. All heavy
 * computations will be migrated to a dedicated read-only reporting database in a future phase; for
 * now they operate against the same database via their module APIs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesAnalyticsService {

  private final OrderApi orderApi;
  private final CatalogApi catalogApi;

  // ── Dashboard stats ────────────────────────────────────────────────────────

  /**
   * Returns high-level statistics for the admin dashboard.
   *
   * <p>Values are placeholders until the Order module exposes aggregate query methods through
   * {@link OrderApi}. The structure is intentionally forward-compatible.
   */
  public DashboardStatsResponse getBasicDashboardStats() {
    log.debug("Computing dashboard stats");
    return new DashboardStatsResponse(
        /* totalOrders   */ 0L,
        /* totalRevenue  */ BigDecimal.ZERO,
        /* currency      */ "USD",
        /* pendingOrders */ 0L,
        /* generatedAt   */ Instant.now());
  }

  // ── Sales summaries ────────────────────────────────────────────────────────

  /**
   * Returns per-day sales summaries for the given date range.
   *
   * <p>Will be implemented with JPQL aggregate queries once the Order module exposes a suitable
   * query. Returns an empty list until then.
   */
  public List<SalesSummaryResponse> getDailySales(Instant from, Instant to) {
    log.debug("Computing daily sales from={} to={}", from, to);
    // TODO: replace with order repository aggregate
    return List.of();
  }

  /** Returns per-month sales summaries for the given year. */
  public List<SalesSummaryResponse> getMonthlySales(int year) {
    log.debug("Computing monthly sales for year={}", year);
    // TODO: replace with order repository aggregate
    return List.of();
  }

  // ── Top products ───────────────────────────────────────────────────────────

  /**
   * Returns the top-selling product IDs by revenue in the given date range.
   *
   * <p>Will query order line items and aggregate by product once OrderApi exposes that data.
   */
  public List<Long> getTopProductIds(Instant from, Instant to, int limit) {
    log.debug("Computing top {} products from={} to={}", limit, from, to);
    // TODO: replace with order-line aggregate query
    return List.of();
  }
}
