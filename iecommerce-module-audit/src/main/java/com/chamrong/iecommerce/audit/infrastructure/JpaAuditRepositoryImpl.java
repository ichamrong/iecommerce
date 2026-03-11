package com.chamrong.iecommerce.audit.infrastructure;

import com.chamrong.iecommerce.audit.application.dto.AuditQuery;
import com.chamrong.iecommerce.audit.domain.AuditEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/** Implements keyset pagination for audit list methods. */
@Component
public class JpaAuditRepositoryImpl implements JpaAuditRepositoryCustom {

  private static final Sort KEYSET_SORT = Sort.by(Sort.Direction.DESC, "createdAt", "id");

  private final EntityManager entityManager;

  public JpaAuditRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<AuditEvent> findFirstPage(String tenantId, int limitPlusOne) {
    Specification<AuditEvent> spec = (root, q, cb) -> cb.equal(root.get("tenantId"), tenantId);
    return execute(spec, limitPlusOne);
  }

  @Override
  public List<AuditEvent> findNextPage(
      String tenantId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne) {
    Specification<AuditEvent> spec = tenantAndKeysetSpec(tenantId, cursorCreatedAt, cursorId);
    return execute(spec, limitPlusOne);
  }

  @Override
  public List<AuditEvent> findFirstPageByUserId(String tenantId, String userId, int limitPlusOne) {
    Specification<AuditEvent> spec =
        (root, q, cb) -> {
          var predicates = new ArrayList<Predicate>();
          if (tenantId != null && !tenantId.isBlank()) {
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
          }
          predicates.add(cb.equal(root.get("userId"), userId));
          return cb.and(predicates.toArray(new Predicate[0]));
        };
    return execute(spec, limitPlusOne);
  }

  @Override
  public List<AuditEvent> findNextPageByUserId(
      String tenantId, String userId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne) {
    Specification<AuditEvent> spec =
        (root, q, cb) -> {
          var predicates = new ArrayList<Predicate>();
          if (tenantId != null && !tenantId.isBlank()) {
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
          }
          predicates.add(cb.equal(root.get("userId"), userId));
          predicates.add(keysetPredicate(root, q, cb, cursorCreatedAt, cursorId));
          return cb.and(predicates.toArray(new Predicate[0]));
        };
    return execute(spec, limitPlusOne);
  }

  @Override
  public List<AuditEvent> findFirstPageByQuery(
      String tenantId, AuditQuery query, int limitPlusOne) {
    Specification<AuditEvent> spec = querySpec(tenantId, query, null, null);
    return execute(spec, limitPlusOne);
  }

  @Override
  public List<AuditEvent> findNextPageByQuery(
      String tenantId, AuditQuery query, Instant cursorCreatedAt, Long cursorId, int limitPlusOne) {
    Specification<AuditEvent> spec = querySpec(tenantId, query, cursorCreatedAt, cursorId);
    return execute(spec, limitPlusOne);
  }

  private List<AuditEvent> execute(Specification<AuditEvent> spec, int limitPlusOne) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<AuditEvent> cq = cb.createQuery(AuditEvent.class);
    Root<AuditEvent> root = cq.from(AuditEvent.class);
    Predicate predicate = spec.toPredicate(root, cq, cb);
    if (predicate != null) {
      cq.where(predicate);
    }
    cq.orderBy(
        KEYSET_SORT.stream()
            .map(
                order ->
                    order.isAscending()
                        ? cb.asc(root.get(order.getProperty()))
                        : cb.desc(root.get(order.getProperty())))
            .toList());
    return entityManager.createQuery(cq).setMaxResults(limitPlusOne).getResultList();
  }

  private static Specification<AuditEvent> tenantAndKeysetSpec(
      String tenantId, Instant cursorCreatedAt, Long cursorId) {
    return (root, q, cb) ->
        cb.and(
            cb.equal(root.get("tenantId"), tenantId),
            keysetPredicate(root, q, cb, cursorCreatedAt, cursorId));
  }

  private static jakarta.persistence.criteria.Predicate keysetPredicate(
      jakarta.persistence.criteria.Root<?> root,
      jakarta.persistence.criteria.CriteriaQuery<?> q,
      jakarta.persistence.criteria.CriteriaBuilder cb,
      Instant cursorCreatedAt,
      Long cursorId) {
    return cb.or(
        cb.lessThan(root.get("createdAt"), cursorCreatedAt),
        cb.and(
            cb.equal(root.get("createdAt"), cursorCreatedAt),
            cb.lessThan(root.get("id"), cursorId)));
  }

  private static Specification<AuditEvent> querySpec(
      String tenantId, AuditQuery query, Instant cursorCreatedAt, Long cursorId) {
    return (root, q, cb) -> {
      var predicates = new ArrayList<Predicate>();
      if (tenantId != null && !tenantId.isBlank()) {
        predicates.add(cb.equal(root.get("tenantId"), tenantId));
      }

      if (query.userId() != null && !query.userId().isBlank()) {
        predicates.add(cb.equal(root.get("userId"), query.userId()));
      }
      if (query.action() != null && !query.action().isBlank()) {
        predicates.add(cb.equal(root.get("action"), query.action()));
      }
      if (query.resourceType() != null && !query.resourceType().isBlank()) {
        predicates.add(cb.equal(root.get("resourceType"), query.resourceType()));
      }
      if (query.resourceId() != null && !query.resourceId().isBlank()) {
        predicates.add(cb.equal(root.get("resourceId"), query.resourceId()));
      }
      if (query.from() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), query.from()));
      }
      if (query.to() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), query.to()));
      }
      if (query.searchTerm() != null && !query.searchTerm().isBlank()) {
        String pattern = "%" + query.searchTerm().toLowerCase() + "%";
        predicates.add(
            cb.or(
                cb.like(cb.lower(root.get("action")), pattern),
                cb.like(cb.lower(root.get("resourceType")), pattern),
                cb.like(cb.lower(root.get("resourceId")), pattern),
                cb.like(cb.lower(root.get("metadata")), pattern)));
      }
      if (cursorCreatedAt != null && cursorId != null) {
        predicates.add(keysetPredicate(root, q, cb, cursorCreatedAt, cursorId));
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
