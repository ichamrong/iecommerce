package com.chamrong.iecommerce.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.chamrong.iecommerce.inventory.application.InventoryService;
import com.chamrong.iecommerce.inventory.application.dto.AdjustStockRequest;
import com.chamrong.iecommerce.inventory.domain.OutOfStockException;
import com.chamrong.iecommerce.inventory.domain.StockLevel;
import com.chamrong.iecommerce.inventory.domain.StockLevelRepository;
import com.chamrong.iecommerce.inventory.domain.StockMovement;
import com.chamrong.iecommerce.inventory.domain.StockMovementRepository;
import com.chamrong.iecommerce.inventory.domain.WarehouseRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

  @Mock private StockLevelRepository stockLevelRepository;
  @Mock private WarehouseRepository warehouseRepository;
  @Mock private StockMovementRepository stockMovementRepository;

  @InjectMocks private InventoryService inventoryService;

  private StockLevel stockLevel;

  @BeforeEach
  void setUp() {
    stockLevel = new StockLevel();
    stockLevel.setId(1L);
    stockLevel.setTenantId("t1");
    stockLevel.setProductId(100L);
    stockLevel.setWarehouseId(200L);
    stockLevel.setQuantity(50);
  }

  @Test
  void testReserveStock_Success() {
    when(stockLevelRepository.findByProductId(100L)).thenReturn(List.of(stockLevel));

    inventoryService.reserveStock("t1", 100L, 10);

    assertEquals(10, stockLevel.getReservedQuantity());
    assertEquals(40, stockLevel.getAvailableQuantity());

    verify(stockLevelRepository).save(stockLevel);
  }

  @Test
  void testReserveStock_NotEnoughStock_ThrowsException() {
    when(stockLevelRepository.findByProductId(100L)).thenReturn(List.of(stockLevel));

    assertThrows(OutOfStockException.class, () -> inventoryService.reserveStock("t1", 100L, 60));

    verify(stockLevelRepository, never()).save(any());
  }

  @Test
  void testReleaseStock_Success() {
    stockLevel.reserve(15);
    when(stockLevelRepository.findByProductId(100L)).thenReturn(List.of(stockLevel));

    inventoryService.releaseStock("t1", 100L, 10);

    assertEquals(5, stockLevel.getReservedQuantity());
    assertEquals(45, stockLevel.getAvailableQuantity());

    verify(stockLevelRepository).save(stockLevel);
  }

  @Test
  void testDeductStock_Success() {
    stockLevel.reserve(10);
    when(stockLevelRepository.findByProductId(100L)).thenReturn(List.of(stockLevel));

    inventoryService.deductStock("t1", 100L, 10);

    // Should deduct from reserved and total quantity
    assertEquals(0, stockLevel.getReservedQuantity());
    assertEquals(40, stockLevel.getQuantity());

    verify(stockLevelRepository).save(stockLevel);

    // Verify movement
    ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
    verify(stockMovementRepository).save(movementCaptor.capture());

    StockMovement mv = movementCaptor.getValue();
    assertEquals(-10, mv.getQuantity());
    assertEquals(StockMovement.MovementReason.SALE, mv.getReason());
    assertEquals("Order shipped", mv.getComment());
  }

  @Test
  void testAdjustStock_Positive() {
    when(stockLevelRepository.findByProductIdAndWarehouseId(100L, 200L))
        .thenReturn(Optional.of(stockLevel));

    inventoryService.adjustStock(
        "t1",
        new AdjustStockRequest(
            100L, 200L, 25, StockMovement.MovementReason.RESTOCK, "Supplier delivery"));

    assertEquals(75, stockLevel.getQuantity());
    verify(stockLevelRepository).save(stockLevel);

    ArgumentCaptor<StockMovement> mvCaptor = ArgumentCaptor.forClass(StockMovement.class);
    verify(stockMovementRepository).save(mvCaptor.capture());

    StockMovement mv = mvCaptor.getValue();
    assertEquals(25, mv.getQuantity());
    assertEquals(StockMovement.MovementReason.RESTOCK, mv.getReason());
  }
}
