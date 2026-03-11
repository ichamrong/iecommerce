package com.chamrong.iecommerce.notification.infrastructure;

import com.chamrong.iecommerce.notification.domain.Notification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Keyset pagination helper for notification listings.
 *
 * <p>Sort order: {@code created_at DESC, id DESC}.
 */
@Repository
public class NotificationKeysetQuery {

  @PersistenceContext private EntityManager entityManager;

  /**
   * Returns a page of notifications for a tenant (or all tenants when tenantId is {@link
   * com.chamrong.iecommerce.common.TenantContext#PLATFORM_ADMIN_SENTINEL}).
   *
   * @param tenantId tenant scope, or PLATFORM_ADMIN_SENTINEL for cross-tenant list
   * @param afterCreatedAt upper bound for createdAt; null for first page
   * @param afterId tie-breaker id; null for first page
   * @param limit page size (callers typically use requested limit + 1 to detect hasNext)
   */
  public List<Notification> findNextPage(
      String tenantId, Instant afterCreatedAt, Long afterId, int limit) {
    boolean allTenants =
        com.chamrong.iecommerce.common.TenantContext.PLATFORM_ADMIN_SENTINEL.equals(tenantId);
    String jpql;
    TypedQuery<Notification> query;
    if (afterCreatedAt == null || afterId == null) {
      if (allTenants) {
        jpql = "SELECT n FROM Notification n ORDER BY n.createdAt DESC, n.id DESC";
        query = entityManager.createQuery(jpql, Notification.class);
      } else {
        jpql =
            "SELECT n FROM Notification n "
                + "WHERE n.tenantId = :tenantId "
                + "ORDER BY n.createdAt DESC, n.id DESC";
        query = entityManager.createQuery(jpql, Notification.class);
        query.setParameter("tenantId", tenantId);
      }
    } else {
      if (allTenants) {
        jpql =
            "SELECT n FROM Notification n "
                + "WHERE (n.createdAt < :createdAt OR (n.createdAt = :createdAt AND n.id < :id)) "
                + "ORDER BY n.createdAt DESC, n.id DESC";
        query = entityManager.createQuery(jpql, Notification.class);
        query.setParameter("createdAt", afterCreatedAt);
        query.setParameter("id", afterId);
      } else {
        jpql =
            "SELECT n FROM Notification n "
                + "WHERE n.tenantId = :tenantId "
                + "AND (n.createdAt < :createdAt OR (n.createdAt = :createdAt AND n.id < :id)) "
                + "ORDER BY n.createdAt DESC, n.id DESC";
        query = entityManager.createQuery(jpql, Notification.class);
        query.setParameter("tenantId", tenantId);
        query.setParameter("createdAt", afterCreatedAt);
        query.setParameter("id", afterId);
      }
    }
    query.setMaxResults(limit);
    return query.getResultList();
  }
}
