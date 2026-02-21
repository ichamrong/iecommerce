package com.chamrong.iecommerce.inventory.infrastructure;

import com.chamrong.iecommerce.inventory.domain.StockMovement;
import com.chamrong.iecommerce.inventory.domain.StockMovementRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link StockMovementRepository} port. */
@Repository
public interface JpaStockMovementRepository
    extends JpaRepository<StockMovement, Long>, StockMovementRepository {
  @Override
  List<StockMovement> findByProductId(Long productId);

  @Override
  List<StockMovement> findByWarehouseId(Long warehouseId);
}
