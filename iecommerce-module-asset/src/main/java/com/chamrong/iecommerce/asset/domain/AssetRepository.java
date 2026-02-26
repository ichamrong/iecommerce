package com.chamrong.iecommerce.asset.domain;

import java.util.List;
import java.util.Optional;

public interface AssetRepository {

  Optional<Asset> findById(Long id);

  Asset save(Asset asset);

  List<Asset> findByType(AssetType type);

  List<Asset> findByTenantIdAndType(String tenantId, AssetType type);

  void deleteById(Long id);

  void delete(Asset asset);

  long countByParentId(Long parentId);

  List<Asset> findByTenantIdAndPathStartingWith(String tenantId, String pathPrefix);

  void deleteByTenantIdAndPathStartingWith(String tenantId, String pathPrefix);

  List<Asset> findByTenantIdAndNameContainingIgnoreCase(String tenantId, String name);

  List<Asset> findByTenantIdAndFileSizeBetween(String tenantId, long minSize, long maxSize);
}
