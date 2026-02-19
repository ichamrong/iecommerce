package com.chamrong.iecommerce.asset.infrastructure;

import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAssetRepository implements AssetRepository {

  private final AssetJpaInterface jpaInterface;

  public JpaAssetRepository(AssetJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public Optional<Asset> findById(Long id) {
    return jpaInterface.findById(id);
  }

  @Override
  public Asset save(Asset asset) {
    return jpaInterface.save(asset);
  }

  public interface AssetJpaInterface extends JpaRepository<Asset, Long> {}
}
