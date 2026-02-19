package com.chamrong.iecommerce.inventory.infrastructure;

import com.chamrong.iecommerce.inventory.domain.StockLevel;
import com.chamrong.iecommerce.inventory.domain.StockLevelRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaStockLevelRepository implements StockLevelRepository {

  private final StockLevelJpaInterface jpaInterface;

  public JpaStockLevelRepository(StockLevelJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public StockLevel save(StockLevel stockLevel) {
    return jpaInterface.save(stockLevel);
  }

  @Override
  public Optional<StockLevel> findByProductIdAndWarehouseId(Long productId, Long warehouseId) {
    return jpaInterface.findByProductIdAndWarehouseId(productId, warehouseId);
  }

  @Override
  public List<StockLevel> findByProductId(Long productId) {
    return jpaInterface.findByProductId(productId);
  }

  public interface StockLevelJpaInterface extends JpaRepository<StockLevel, Long> {
    Optional<StockLevel> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<StockLevel> findByProductId(Long productId);
  }
}
