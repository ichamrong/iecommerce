package com.chamrong.iecommerce.asset.infrastructure;

import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.AssetType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link AssetRepository} port. */
@Repository
public interface JpaAssetRepository extends JpaRepository<Asset, Long>, AssetRepository {

  @Override
  List<Asset> findByTenantIdAndType(String tenantId, AssetType type);

  @Override
  List<Asset> findByType(AssetType type);

  @Override
  long countByParentId(Long parentId);

  @Override
  List<Asset> findByTenantIdAndPathStartingWith(String tenantId, String pathPrefix);

  @Override
  void deleteByTenantIdAndPathStartingWith(String tenantId, String pathPrefix);

  @Override
  List<Asset> findByTenantIdAndNameContainingIgnoreCase(String tenantId, String name);

  @Override
  List<Asset> findByTenantIdAndFileSizeBetween(String tenantId, long minSize, long maxSize);
}
