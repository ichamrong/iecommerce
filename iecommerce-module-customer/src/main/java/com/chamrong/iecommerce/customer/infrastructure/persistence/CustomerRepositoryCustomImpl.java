package com.chamrong.iecommerce.customer.infrastructure.persistence;

import com.chamrong.iecommerce.customer.api.util.CursorEncoder.Cursor;
import com.chamrong.iecommerce.customer.domain.Customer;
import com.chamrong.iecommerce.customer.domain.CustomerRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

public class CustomerRepositoryCustomImpl implements CustomerRepositoryCustom {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public List<Customer> findNextPage(String tenantId, Cursor cursor, int limit) {
    String jpql;
    if (cursor == null) {
      jpql =
          "SELECT c FROM Customer c WHERE c.tenantId = :tenantId "
              + "ORDER BY c.createdAt DESC, c.id DESC";
      return entityManager
          .createQuery(jpql, Customer.class)
          .setParameter("tenantId", tenantId)
          .setMaxResults(limit)
          .getResultList();
    } else {
      jpql =
          "SELECT c FROM Customer c WHERE c.tenantId = :tenantId "
              + "AND (c.createdAt < :createdAt OR (c.createdAt = :createdAt AND c.id < :id)) "
              + "ORDER BY c.createdAt DESC, c.id DESC";
      return entityManager
          .createQuery(jpql, Customer.class)
          .setParameter("tenantId", tenantId)
          .setParameter("createdAt", cursor.createdAt())
          .setParameter("id", cursor.id())
          .setMaxResults(limit)
          .getResultList();
    }
  }

  @Override
  public List<Customer> findAllByTenantId(String tenantId) {
    String jpql =
        "SELECT c FROM Customer c WHERE c.tenantId = :tenantId ORDER BY c.createdAt DESC, c.id"
            + " DESC";
    return entityManager
        .createQuery(jpql, Customer.class)
        .setParameter("tenantId", tenantId)
        .getResultList();
  }
}
