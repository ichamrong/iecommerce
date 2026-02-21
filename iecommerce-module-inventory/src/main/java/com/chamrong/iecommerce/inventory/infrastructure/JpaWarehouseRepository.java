package com.chamrong.iecommerce.inventory.infrastructure;

import com.chamrong.iecommerce.inventory.domain.Warehouse;
import com.chamrong.iecommerce.inventory.domain.WarehouseRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link WarehouseRepository} port. */
@Repository
public interface JpaWarehouseRepository
    extends JpaRepository<Warehouse, Long>, WarehouseRepository {}
