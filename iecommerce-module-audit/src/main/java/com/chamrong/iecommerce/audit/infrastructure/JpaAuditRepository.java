package com.chamrong.iecommerce.audit.infrastructure;

import com.chamrong.iecommerce.audit.application.dto.AuditQuery;
import com.chamrong.iecommerce.audit.domain.AuditEvent;
import com.chamrong.iecommerce.audit.domain.AuditRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link AuditRepository} port. */
@Repository
public interface JpaAuditRepository
    extends JpaRepository<AuditEvent, Long>, JpaSpecificationExecutor<AuditEvent>, AuditRepository {

  @Override
  Page<AuditEvent> findByUserId(String userId, Pageable pageable);

  @Override
  @Query("SELECT DISTINCT e.action FROM AuditEvent e ORDER BY e.action")
  List<String> findUniqueActions();

  @Override
  @Query("SELECT DISTINCT e.resourceType FROM AuditEvent e ORDER BY e.resourceType")
  List<String> findUniqueResourceTypes();

  @Override
  default Page<AuditEvent> findByQuery(AuditQuery query, Pageable pageable) {
    Specification<AuditEvent> spec =
        (root, q, cb) -> {
          var predicates = new ArrayList<Predicate>();

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
            var searchPredicates = new ArrayList<Predicate>();
            searchPredicates.add(cb.like(cb.lower(root.get("action")), pattern));
            searchPredicates.add(cb.like(cb.lower(root.get("resourceType")), pattern));
            searchPredicates.add(cb.like(cb.lower(root.get("resourceId")), pattern));
            searchPredicates.add(cb.like(cb.lower(root.get("metadata")), pattern));
            predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
          }

          return cb.and(predicates.toArray(new Predicate[0]));
        };

    return findAll(spec, pageable);
  }
}
