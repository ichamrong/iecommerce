package com.chamrong.iecommerce.audit.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.audit.domain.ports.AuditSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA specifications for audit_event: tenant-scoped keyset and optional filters.
 */
public final class AuditQuerySpecifications {

  private AuditQuerySpecifications() {}

  public static Specification<AuditEventEntity> withTenantAndFilters(
      String tenantId, AuditSearchCriteria criteria) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("tenantId"), tenantId));
      if (criteria != null) {
        if (criteria.actorId() != null && !criteria.actorId().isBlank()) {
          predicates.add(cb.equal(root.get("actorId"), criteria.actorId()));
        }
        if (criteria.eventType() != null && !criteria.eventType().isBlank()) {
          predicates.add(cb.equal(root.get("eventType"), criteria.eventType()));
        }
        if (criteria.outcome() != null && !criteria.outcome().isBlank()) {
          predicates.add(cb.equal(root.get("outcome"), criteria.outcome()));
        }
        if (criteria.severity() != null && !criteria.severity().isBlank()) {
          predicates.add(cb.equal(root.get("severity"), criteria.severity()));
        }
        if (criteria.targetType() != null && !criteria.targetType().isBlank()) {
          predicates.add(cb.equal(root.get("targetType"), criteria.targetType()));
        }
        if (criteria.targetId() != null && !criteria.targetId().isBlank()) {
          predicates.add(cb.equal(root.get("targetId"), criteria.targetId()));
        }
        if (criteria.dateFrom() != null) {
          predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.dateFrom()));
        }
        if (criteria.dateTo() != null) {
          predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.dateTo()));
        }
        if (criteria.searchTerm() != null && !criteria.searchTerm().isBlank()) {
          String pattern = "%" + criteria.searchTerm().toLowerCase() + "%";
          predicates.add(
              cb.or(
                  cb.like(cb.lower(root.get("eventType")), pattern),
                  cb.like(cb.lower(root.get("metadataJson")), pattern)));
        }
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  public static Specification<AuditEventEntity> keysetBefore(
      Instant cursorCreatedAt, Long cursorId) {
    return (root, query, cb) ->
        cb.or(
            cb.lessThan(root.get("createdAt"), cursorCreatedAt),
            cb.and(
                cb.equal(root.get("createdAt"), cursorCreatedAt),
                cb.lessThan(root.get("id"), cursorId)));
  }
}
