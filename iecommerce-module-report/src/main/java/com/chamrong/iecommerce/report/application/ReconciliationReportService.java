package com.chamrong.iecommerce.report.application;

import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceRepository;
import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderRepository;
import com.chamrong.iecommerce.order.domain.OrderState;
import com.chamrong.iecommerce.payment.domain.Payment;
import com.chamrong.iecommerce.payment.domain.PaymentRepository;
import com.chamrong.iecommerce.report.application.dto.PosReconciliationDto;
import com.chamrong.iecommerce.report.application.dto.TenantReconciliationResultDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationReportService {

  private final OrderRepository orderRepository;
  private final InvoiceRepository invoiceRepository;
  private final PaymentRepository paymentRepository;

  /**
   * Generates a reconciliation report for a specific POS terminal session. This bridges Order and
   * Auth modules conceptually, but operates as a stub for Phase 5.
   */
  public PosReconciliationDto generateSessionReport(
      String tenantId, Long terminalId, Long cashierId, BigDecimal actualCashCounted) {

    // Stub implementation for Phase 5 POS infrastructure binding.
    // In production, this would scan the "ecommerce_order" and "payment_transaction" tables
    // filtering by orders placed during the specific auth_pos_session timelines.

    log.info(
        "Generating POS Reconciliation Report for terminal={} cashier={} tenant={}",
        terminalId,
        cashierId,
        tenantId);

    List<PosReconciliationDto.PosTransactionDto> transactions = new ArrayList<>();
    transactions.add(
        PosReconciliationDto.PosTransactionDto.builder()
            .orderId(101L)
            .amount(new BigDecimal("25.50"))
            .method("CASH")
            .timestamp(Instant.now().minusSeconds(3600))
            .build());
    transactions.add(
        PosReconciliationDto.PosTransactionDto.builder()
            .orderId(105L)
            .amount(new BigDecimal("15.00"))
            .method("CASH")
            .timestamp(Instant.now().minusSeconds(1800))
            .build());

    BigDecimal expectedCash = new BigDecimal("40.50");
    BigDecimal discrepancy = actualCashCounted.subtract(expectedCash);

    return PosReconciliationDto.builder()
        .terminalId(terminalId)
        .cashierId(cashierId)
        .sessionOpenedAt(Instant.now().minusSeconds(28800)) // 8 hours ago
        .sessionClosedAt(Instant.now())
        .expectedCashInDrawer(expectedCash)
        .actualCashCounted(actualCashCounted)
        .discrepancy(discrepancy)
        .totalTransactionCount(2)
        .transactions(transactions)
        .build();
  }

  /**
   * Reconciles Tenant Financials (Sum of Orders == Sum of Invoices == Sum of Payments). Verifies
   * consistency across multiple financial and operational modules.
   */
  public TenantReconciliationResultDto reconcileTenantFinancials(
      String tenantId, Instant start, Instant end) {
    log.info(
        "Starting financial reconciliation for tenant={}, from={}, to={}", tenantId, start, end);

    List<Order> orders = orderRepository.findByTenantIdAndCreatedAtBetween(tenantId, start, end);
    List<Invoice> invoices =
        invoiceRepository.findByTenantIdAndCreatedAtBetween(tenantId, start, end);
    List<Payment> payments =
        paymentRepository.findByTenantIdAndCreatedAtBetween(tenantId, start, end);

    // Group invoices by orderId
    Map<Long, BigDecimal> invoicedSums =
        invoices.stream()
            .collect(
                Collectors.groupingBy(
                    Invoice::getOrderId,
                    Collectors.mapping(
                        inv ->
                            inv.getTotalAmount() != null
                                ? inv.getTotalAmount().getAmount()
                                : BigDecimal.ZERO,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

    // Group payments by orderId
    Map<Long, BigDecimal> paidSums =
        payments.stream()
            .collect(
                Collectors.groupingBy(
                    Payment::getOrderId,
                    Collectors.mapping(
                        pay ->
                            pay.getAmount() != null ? pay.getAmount().getAmount() : BigDecimal.ZERO,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

    BigDecimal totalOrders = BigDecimal.ZERO;
    BigDecimal totalInvoices = BigDecimal.ZERO;
    BigDecimal totalPayments = BigDecimal.ZERO;

    List<TenantReconciliationResultDto.MismatchDetail> mismatches = new ArrayList<>();

    for (Order order : orders) {
      if (order.getState() == OrderState.AddingItems || order.getState() == OrderState.Cancelled) {
        continue; // Only reconcile confirmed, shipped, or completed orders
      }

      BigDecimal orderTotal =
          order.getTotal() != null && order.getTotal().getAmount() != null
              ? order.getTotal().getAmount()
              : BigDecimal.ZERO;
      BigDecimal invoiceTotal = invoicedSums.getOrDefault(order.getId(), BigDecimal.ZERO);
      BigDecimal paymentTotal = paidSums.getOrDefault(order.getId(), BigDecimal.ZERO);

      totalOrders = totalOrders.add(orderTotal);
      totalInvoices = totalInvoices.add(invoiceTotal);
      totalPayments = totalPayments.add(paymentTotal);

      if (orderTotal.compareTo(invoiceTotal) != 0 || orderTotal.compareTo(paymentTotal) != 0) {
        String status = "MISMATCH";
        if (orderTotal.compareTo(invoiceTotal) > 0) status = "UNINVOICED_OR_UNDER_INVOICED";
        else if (orderTotal.compareTo(paymentTotal) > 0) status = "UNPAID_OR_UNDERPAID";
        else if (orderTotal.compareTo(paymentTotal) < 0) status = "OVERPAID";

        mismatches.add(
            TenantReconciliationResultDto.MismatchDetail.builder()
                .orderId(order.getId())
                .orderCode(order.getCode())
                .orderTotal(orderTotal)
                .invoiceTotal(invoiceTotal)
                .paymentTotal(paymentTotal)
                .status(status)
                .build());
      }
    }

    return TenantReconciliationResultDto.builder()
        .tenantId(tenantId)
        .periodStart(start)
        .periodEnd(end)
        .totalOrdersAnalyzed(orders.size())
        .totalMismatchesFound(mismatches.size())
        .sumOfOrders(totalOrders)
        .sumOfInvoices(totalInvoices)
        .sumOfPayments(totalPayments)
        .totalDiscrepancyOrdersVsInvoices(totalOrders.subtract(totalInvoices).abs())
        .totalDiscrepancyOrdersVsPayments(totalOrders.subtract(totalPayments).abs())
        .mismatches(mismatches)
        .build();
  }
}
