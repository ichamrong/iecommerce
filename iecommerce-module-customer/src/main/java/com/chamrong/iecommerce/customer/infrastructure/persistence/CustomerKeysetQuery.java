package com.chamrong.iecommerce.customer.infrastructure.persistence;

import com.chamrong.iecommerce.customer.domain.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

/** Keyset pagination for customer list. Used by {@link JpaCustomerRepositoryAdapter}. */
@Repository
public class CustomerKeysetQuery {

  @PersistenceContext private EntityManager entityManager;

  /**
   * Returns a page of customers. Order: created_at DESC, id DESC.
   *
   * @param afterCreatedAt null for first page
   * @param afterId null for first page
   */
  public List<Customer> findNextPage(
      String tenantId, Instant afterCreatedAt, Long afterId, int limit) {
    if (afterCreatedAt == null || afterId == null) {
      String jpql =
          "SELECT c FROM Customer c WHERE c.tenantId = :tenantId "
              + "ORDER BY c.createdAt DESC, c.id DESC";
      return entityManager
          .createQuery(jpql, Customer.class)
          .setParameter("tenantId", tenantId)
          .setMaxResults(limit)
          .getResultList();
    }
    String jpql =
        "SELECT c FROM Customer c WHERE c.tenantId = :tenantId "
            + "AND (c.createdAt < :createdAt OR (c.createdAt = :createdAt AND c.id < :id)) "
            + "ORDER BY c.createdAt DESC, c.id DESC";
    return entityManager
        .createQuery(jpql, Customer.class)
        .setParameter("tenantId", tenantId)
        .setParameter("createdAt", afterCreatedAt)
        .setParameter("id", afterId)
        .setMaxResults(limit)
        .getResultList();
  }
}
