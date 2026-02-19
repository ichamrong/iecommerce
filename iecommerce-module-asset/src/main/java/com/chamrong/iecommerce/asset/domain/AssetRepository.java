package com.chamrong.iecommerce.asset.domain;

import java.util.Optional;

public interface AssetRepository {
  Optional<Asset> findById(Long id);

  Asset save(Asset asset);
}
