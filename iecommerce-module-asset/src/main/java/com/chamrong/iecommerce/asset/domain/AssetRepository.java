package com.chamrong.iecommerce.asset.domain;

import java.util.List;
import java.util.Optional;

public interface AssetRepository {
  Optional<Asset> findById(Long id);

  Asset save(Asset asset);

  List<Asset> findByType(AssetType type);

  void deleteById(Long id);
}
