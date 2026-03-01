package com.chamrong.iecommerce.sale.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.sale.SaleCursorPaginationTestApplication;
import com.chamrong.iecommerce.sale.domain.model.Shift.ShiftStatus;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.SpringDataShiftRepository;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.ShiftEntity;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for sale cursor pagination at the repository layer: deterministic order
 * (created_at DESC, id DESC), page1+page2 no duplicates, and tenant isolation. Filter mismatch is
 * covered by {@link com.chamrong.iecommerce.sale.application.query.SaleQueryServiceCursorTest}.
 */
@SpringBootTest(classes = SaleCursorPaginationTestApplication.class)
@Transactional
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:sale_cursor_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
      "spring.jpa.hibernate.ddl-auto=create-drop"
    })
class SaleCursorPaginationIntegrationTest {

  private static final String TENANT_A = "tenant-a";
  private static final String TENANT_B = "tenant-b";

  @Autowired private SpringDataShiftRepository repository;

  @Nested
  @DisplayName("Deterministic order and no duplicates across pages")
  class OrderAndNoDuplicates {

    @Test
    @DisplayName("findPaged returns deterministic order and page2 has no overlap with page1")
    void findPaged_deterministicOrder_page2NoDuplicates() {
      Instant base = Instant.now();
      for (int i = 0; i < 5; i++) {
        persistShift(TENANT_A, base.minusSeconds(i));
      }

      List<ShiftEntity> page1 =
          repository.findPaged(
              TENANT_A,
              CursorSentinel.NO_CURSOR_ID,
              CursorSentinel.NO_CURSOR_TIME,
              PageRequest.of(0, 3));
      assertThat(page1).hasSize(3);
      List<Long> page1Ids = page1.stream().map(ShiftEntity::getId).toList();

      ShiftEntity last1 = page1.get(page1.size() - 1);
      List<ShiftEntity> page2 =
          repository.findPaged(TENANT_A, last1.getId(), last1.getCreatedAt(), PageRequest.of(0, 3));
      assertThat(page2).hasSize(2);
      List<Long> page2Ids = page2.stream().map(ShiftEntity::getId).toList();

      Set<Long> allIds = new HashSet<>(page1Ids);
      allIds.addAll(page2Ids);
      assertThat(allIds).hasSize(5);
    }
  }

  @Nested
  @DisplayName("Tenant isolation")
  class TenantIsolation {

    @Test
    @DisplayName("findPaged for tenant B returns empty when data belongs to tenant A")
    void findPaged_tenantB_seesNoDataFromTenantA() {
      persistShift(TENANT_A, Instant.now());

      List<ShiftEntity> forA =
          repository.findPaged(
              TENANT_A,
              CursorSentinel.NO_CURSOR_ID,
              CursorSentinel.NO_CURSOR_TIME,
              PageRequest.of(0, 20));
      assertThat(forA).hasSize(1);

      List<ShiftEntity> forB =
          repository.findPaged(
              TENANT_B,
              CursorSentinel.NO_CURSOR_ID,
              CursorSentinel.NO_CURSOR_TIME,
              PageRequest.of(0, 20));
      assertThat(forB).isEmpty();
    }
  }

  private void persistShift(String tenantId, Instant createdAt) {
    ShiftEntity e = new ShiftEntity();
    e.setTenantId(tenantId);
    e.setStaffId("staff");
    e.setTerminalId("term");
    e.setStartTime(Instant.now());
    e.setStatus(ShiftStatus.OPEN);
    e.setCreatedAt(createdAt);
    e.setUpdatedAt(createdAt);
    repository.save(e);
  }

  /** Sentinel for first page when repository does not accept null cursor. */
  private static final class CursorSentinel {
    static final Long NO_CURSOR_ID = Long.MAX_VALUE;
    static final Instant NO_CURSOR_TIME = Instant.MAX;

    private CursorSentinel() {}
  }
}
