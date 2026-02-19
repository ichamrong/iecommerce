import com.chamrong.iecommerce.inventory.domain.StockLevel;
import com.chamrong.iecommerce.inventory.domain.StockLevelRepository;
import com.chamrong.iecommerce.inventory.domain.StockMovement;
import com.chamrong.iecommerce.inventory.domain.StockMovementRepository;
import com.chamrong.iecommerce.inventory.domain.Warehouse;
import com.chamrong.iecommerce.inventory.domain.WarehouseRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

  private final StockLevelRepository stockLevelRepository;
  private final WarehouseRepository warehouseRepository;
  private final StockMovementRepository stockMovementRepository;

  public InventoryService(
      StockLevelRepository stockLevelRepository,
      WarehouseRepository warehouseRepository,
      StockMovementRepository stockMovementRepository) {
    this.stockLevelRepository = stockLevelRepository;
    this.warehouseRepository = warehouseRepository;
    this.stockMovementRepository = stockMovementRepository;
  }

  @Transactional
  public Warehouse createWarehouse(Warehouse warehouse) {
    return warehouseRepository.save(warehouse);
  }

  @Transactional
  public void adjustStock(
      Long productId,
      Long warehouseId,
      Integer delta,
      StockMovement.MovementReason reason,
      String comment) {
    StockLevel stockLevel =
        stockLevelRepository
            .findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseGet(
                () -> {
                  StockLevel newLevel = new StockLevel();
                  newLevel.setProductId(productId);
                  newLevel.setWarehouseId(warehouseId);
                  newLevel.setQuantity(0);
                  return newLevel;
                });

    stockLevel.setQuantity(stockLevel.getQuantity() + delta);
    stockLevelRepository.save(stockLevel);

    StockMovement movement = new StockMovement();
    movement.setProductId(productId);
    movement.setWarehouseId(warehouseId);
    movement.setQuantity(delta);
    movement.setReason(reason);
    movement.setComment(comment);
    stockMovementRepository.save(movement);
  }

  @Transactional
  public void updateStock(Long productId, Long warehouseId, Integer newQuantity) {
    StockLevel stockLevel =
        stockLevelRepository
            .findByProductIdAndWarehouseId(productId, warehouseId)
            .orElse(new StockLevel());

    int delta = newQuantity - (stockLevel.getQuantity() != null ? stockLevel.getQuantity() : 0);

    stockLevel.setProductId(productId);
    stockLevel.setWarehouseId(warehouseId);
    stockLevel.setQuantity(newQuantity);
    stockLevelRepository.save(stockLevel);

    if (delta != 0) {
      StockMovement movement = new StockMovement();
      movement.setProductId(productId);
      movement.setWarehouseId(warehouseId);
      movement.setQuantity(delta);
      movement.setReason(StockMovement.MovementReason.CORRECTION);
      movement.setComment("Manual stock override");
      stockMovementRepository.save(movement);
    }
  }

  public List<StockLevel> getProductStock(Long productId) {
    return stockLevelRepository.findByProductId(productId);
  }

  public List<Warehouse> getAllWarehouses() {
    return warehouseRepository.findAll();
  }
}
