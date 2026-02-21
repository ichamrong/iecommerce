package com.chamrong.iecommerce.auth.infrastructure.persistence;

import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.PermissionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link PermissionRepository} port. */
@Repository
public interface JpaPermissionRepository
    extends JpaRepository<Permission, Long>, PermissionRepository {}
