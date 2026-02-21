package com.chamrong.iecommerce.staff.infrastructure.persistence;

import com.chamrong.iecommerce.staff.domain.StaffProfile;
import com.chamrong.iecommerce.staff.domain.StaffProfileRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link StaffProfileRepository} port. */
@Repository
public interface JpaStaffProfileRepository
    extends JpaRepository<StaffProfile, Long>, StaffProfileRepository {}
