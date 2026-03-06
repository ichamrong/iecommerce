package com.chamrong.iecommerce.asset.infrastructure.persistence;

import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.AssetType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link AssetRepository} port. */
@Repository
public interface JpaAssetRepository extends JpaRepository<Asset, Long>, AssetRepository {

  @Override
  Optional<Asset> findByIdAndDeletedAtIsNull(Long id);

  @Override
  Optional<Asset> findByTenantIdAndIdAndDeletedAtIsNull(String tenantId, Long id);

  @Override
  List<Asset> findByTenantIdAndTypeAndDeletedAtIsNull(String tenantId, AssetType type);

  @Override
  List<Asset> findByType(AssetType type);

  @Override
  long countByParentId(Long parentId);

  @Override
  List<Asset> findByTenantIdAndPathStartingWithAndDeletedAtIsNull(
      String tenantId, String pathPrefix);

  @Override
  void deleteByTenantIdAndPathStartingWith(String tenantId, String pathPrefix);

  @Override
  List<Asset> findByTenantIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(
      String tenantId, String name);

  @Override
  List<Asset> findByTenantIdAndFileSizeBetweenAndDeletedAtIsNull(
      String tenantId, long minSize, long maxSize);

  @Override
  @org.springframework.data.jpa.repository.Query(
      "SELECT COALESCE(SUM(a.fileSize), 0) FROM Asset a WHERE a.tenantId = :tenantId AND"
          + " a.deletedAt IS NULL")
  long sumFileSizeByTenantIdAndDeletedAtIsNull(String tenantId);
}
