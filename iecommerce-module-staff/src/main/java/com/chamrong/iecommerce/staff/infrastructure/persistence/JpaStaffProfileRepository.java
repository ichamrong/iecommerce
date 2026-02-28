package com.chamrong.iecommerce.staff.infrastructure.persistence;

import com.chamrong.iecommerce.staff.domain.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Legacy adapter — kept for backward compatibility during rollout. All business logic now goes
 * through {@link StaffRepositoryAdapter}.
 *
 * @deprecated Use {@link StaffRepositoryAdapter} via {@link
 *     com.chamrong.iecommerce.staff.domain.StaffRepositoryPort}
 */
@Repository
@Deprecated(since = "v15", forRemoval = true)
public interface JpaStaffProfileRepository extends JpaRepository<StaffProfile, Long> {}
