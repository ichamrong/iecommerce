package com.chamrong.iecommerce.staff.infrastructure.persistence;

import com.chamrong.iecommerce.staff.domain.StaffProfile;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA backing repository for StaffProfile. */
@Repository
interface SpringDataStaffRepository extends JpaRepository<StaffProfile, Long> {

  Optional<StaffProfile> findByUserId(String userId);

  boolean existsByUserId(String userId);

  /**
   * First page — no cursor; Pageable enforces the LIMIT.
   *
   * <p>Uses {@code idx_staff_cursor} index.
   */
  @Query("SELECT s FROM StaffProfile s ORDER BY s.createdAt DESC, s.id DESC")
  List<StaffProfile> findFirstPage(Pageable pageable);

  /**
   * Subsequent pages — keyset after (createdAt, id).
   *
   * <p>The OR clause handles the tie-break when two rows have the same createdAt.
   */
  @Query(
      "SELECT s FROM StaffProfile s "
          + "WHERE s.createdAt < :createdAt "
          + "   OR (s.createdAt = :createdAt AND s.id < :id) "
          + "ORDER BY s.createdAt DESC, s.id DESC")
  List<StaffProfile> findNextPage(
      @Param("createdAt") Instant createdAt, @Param("id") Long id, Pageable pageable);
}
