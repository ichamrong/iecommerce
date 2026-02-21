package com.chamrong.iecommerce.asset.infrastructure;

import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link AssetRepository} port. */
@Repository
public interface JpaAssetRepository extends JpaRepository<Asset, Long>, AssetRepository {}
