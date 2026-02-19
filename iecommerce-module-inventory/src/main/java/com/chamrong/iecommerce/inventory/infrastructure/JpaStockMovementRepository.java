package com.chamrong.iecommerce.inventory.infrastructure;

import com.chamrong.iecommerce.inventory.domain.StockMovement;
import com.chamrong.iecommerce.inventory.domain.StockMovementRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaStockMovementRepository implements StockMovementRepository {

  private final StockMovementJpaInterface jpaInterface;

  public JpaStockMovementRepository(StockMovementJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public StockMovement save(StockMovement movement) {
    return jpaInterface.save(movement);
  }

  @Override
  public List<StockMovement> findByProductId(Long productId) {
    return jpaInterface.findByProductId(productId);
  }

  @Override
  public List<StockMovement> findByWarehouseId(Long warehouseId) {
    return jpaInterface.findByWarehouseId(warehouseId);
  }

  public interface StockMovementJpaInterface extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductId(Long productId);

    List<StockMovement> findByWarehouseId(Long warehouseId);
  }
}
