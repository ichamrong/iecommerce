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
  public List<Customer> findNextPage(Cursor cursor, int limit) {
    String jpql;
    if (cursor == null) {
      // First page, just get the latest
      jpql = "SELECT c FROM Customer c ORDER BY c.createdAt DESC, c.id DESC";
      return entityManager.createQuery(jpql, Customer.class).setMaxResults(limit).getResultList();
    } else {
      // Fetch after cursor
      jpql =
          "SELECT c FROM Customer c "
              + "WHERE c.createdAt < :createdAt "
              + "OR (c.createdAt = :createdAt AND c.id < :id) "
              + "ORDER BY c.createdAt DESC, c.id DESC";
      return entityManager
          .createQuery(jpql, Customer.class)
          .setParameter("createdAt", cursor.createdAt())
          .setParameter("id", cursor.id())
          .setMaxResults(limit)
          .getResultList();
    }
  }
}
