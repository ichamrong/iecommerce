package com.chamrong.iecommerce.asset.domain;

import java.util.List;
import java.util.Optional;

public interface AssetRepository {

  Optional<Asset> findByIdAndDeletedAtIsNull(Long id);

  Asset save(Asset asset);

  <S extends Asset> List<S> saveAll(Iterable<S> entities);

  List<Asset> findByType(AssetType type);

  List<Asset> findByTenantIdAndTypeAndDeletedAtIsNull(String tenantId, AssetType type);

  void deleteById(Long id);

  void delete(Asset asset);

  void deleteAll(Iterable<? extends Asset> entities);

  long countByParentId(Long parentId);

  List<Asset> findByTenantIdAndPathStartingWithAndDeletedAtIsNull(
      String tenantId, String pathPrefix);

  void deleteByTenantIdAndPathStartingWith(String tenantId, String pathPrefix);

  List<Asset> findByTenantIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(
      String tenantId, String name);

  List<Asset> findByTenantIdAndFileSizeBetweenAndDeletedAtIsNull(
      String tenantId, long minSize, long maxSize);

  long sumFileSizeByTenantIdAndDeletedAtIsNull(String tenantId);
}
