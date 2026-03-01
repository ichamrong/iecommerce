package com.chamrong.iecommerce.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.catalog.CatalogApi;
import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.ports.OrderAuditPort;
import com.chamrong.iecommerce.order.domain.ports.OrderOutboxPort;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
import com.chamrong.iecommerce.promotion.PromotionApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

/**
 * TenantGuard on get-by-id: Tenant A cannot read order of Tenant B (AUDIT_REMEDIATION_PLAN Tests to
 * Add). Verifies that OrderService.getOrder returns 404 when the order belongs to another tenant.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTenantGuardTest {

  private static final Long ORDER_ID = 100L;

  @Mock private OrderRepositoryPort orderRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private PromotionApi promotionApi;
  @Mock private CatalogApi catalogApi;
  @Mock private OrderAuditPort auditLogRepository;
  @Mock private OrderOutboxPort outboxRepository;
  @Mock private ObjectMapper objectMapper;
  @Mock private Order orderTenantA;

  @InjectMocks private OrderService orderService;

  @BeforeEach
  void setUp() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderTenantA));
    when(orderTenantA.getTenantId()).thenReturn("tenant-A");
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void getOrder_whenOrderBelongsToDifferentTenant_throws404() {
    TenantContext.setCurrentTenant("tenant-B");

    assertThatThrownBy(() -> orderService.getOrder(ORDER_ID))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex ->
                assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(404));
  }
}
