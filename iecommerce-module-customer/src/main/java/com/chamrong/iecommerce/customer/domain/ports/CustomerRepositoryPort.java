package com.chamrong.iecommerce.customer.domain.ports;

import com.chamrong.iecommerce.customer.domain.Customer;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Port for customer persistence. All operations are tenant-scoped; callers pass tenantId. */
public interface CustomerRepositoryPort {

  Customer save(Customer customer);

  Optional<Customer> findById(Long id);

  Optional<Customer> findByTenantIdAndEmail(String tenantId, String email);

  Optional<Customer> findByAuthUserId(Long authUserId);

  /**
   * Keyset paginated list. Sort: created_at DESC, id DESC.
   *
   * @param tenantId tenant scope
   * @param afterCreatedAt null for first page
   * @param afterId null for first page
   * @param limit fetch size (typically page size + 1 to detect hasNext)
   */
  List<Customer> findCursorPage(String tenantId, Instant afterCreatedAt, Long afterId, int limit);

  /** All customers for tenant (e.g. for admin list). Prefer cursor pagination for large sets. */
  List<Customer> findAllByTenantId(String tenantId);
}
