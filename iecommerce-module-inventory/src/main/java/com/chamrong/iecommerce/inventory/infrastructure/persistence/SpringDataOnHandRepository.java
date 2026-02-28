package com.chamrong.iecommerce.inventory.infrastructure.persistence;

import com.chamrong.iecommerce.inventory.domain.InventoryItem;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA backing repository for {@link InventoryItem} (on-hand projection).
 *
 * <p>The {@link #findForUpdate} method issues a {@code SELECT ... FOR UPDATE} SQL statement via
 * {@code @Lock(LockModeType.PESSIMISTIC_WRITE)}. This acquires a row-level lock that blocks other
 * transactions from updating the same row until the current transaction commits or rolls back.
 *
 * <p>Why pessimistic over optimistic? High-concurrency reservation scenarios (e.g. flash sale) with
 * optimistic locking would generate excessive retry storms. Pessimistic locking serializes access
 * at the DB level, keeping reservation correctness guarantees simpler.
 */
@Repository
interface SpringDataOnHandRepository extends JpaRepository<InventoryItem, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT i FROM InventoryItem i WHERE i.tenantId = :tenantId"
          + " AND i.productId = :productId AND i.warehouseId = :warehouseId")
  Optional<InventoryItem> findForUpdate(
      @Param("tenantId") String tenantId,
      @Param("productId") Long productId,
      @Param("warehouseId") Long warehouseId);

  @Query(
      "SELECT i FROM InventoryItem i WHERE i.tenantId = :tenantId"
          + " AND i.productId = :productId AND i.warehouseId = :warehouseId")
  Optional<InventoryItem> findByProjection(
      @Param("tenantId") String tenantId,
      @Param("productId") Long productId,
      @Param("warehouseId") Long warehouseId);

  @Query("SELECT i FROM InventoryItem i WHERE i.tenantId = :tenantId AND i.productId = :productId")
  List<InventoryItem> findAllByProduct(
      @Param("tenantId") String tenantId, @Param("productId") Long productId);

  // Backward compat — used by existing InventoryService
  @Query("SELECT i FROM InventoryItem i WHERE i.productId = :productId")
  List<InventoryItem> findByProductId(@Param("productId") Long productId);

  @Query("SELECT i FROM InventoryItem i WHERE i.warehouseId = :warehouseId")
  List<InventoryItem> findByWarehouseId(@Param("warehouseId") Long warehouseId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT i FROM InventoryItem i WHERE i.productId = :productId")
  List<InventoryItem> findForUpdateByProductId(@Param("productId") Long productId);

  @Query("SELECT i FROM InventoryItem i WHERE i.tenantId = :tenantId AND i.onHandQty <= :threshold")
  List<InventoryItem> findLowStock(
      @Param("tenantId") String tenantId, @Param("threshold") int threshold);
}
