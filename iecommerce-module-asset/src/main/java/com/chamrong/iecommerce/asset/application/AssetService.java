package com.chamrong.iecommerce.asset.application;

import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetService {

  private final AssetRepository assetRepository;

  public AssetService(AssetRepository assetRepository) {
    this.assetRepository = assetRepository;
  }

  @Transactional
  public Asset createAsset(Asset asset) {
    return assetRepository.save(asset);
  }

  public Optional<Asset> getAsset(Long id) {
    return assetRepository.findById(id);
  }
}
