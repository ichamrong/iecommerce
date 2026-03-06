package com.chamrong.iecommerce.asset.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.asset.application.service.AssetRetrievalService;
import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.AssetType;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.AssetException;
import com.chamrong.iecommerce.common.EventDispatcher;
import com.chamrong.iecommerce.common.TenantContext;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssetRetrievalServiceMultiTenantTest {

  @Mock private AssetRepository assetRepository;
  @Mock private StorageService storageService;
  @Mock private EventDispatcher eventDispatcher;

  private AssetRetrievalService retrievalService;

  @BeforeEach
  void setUp() {
    retrievalService = new AssetRetrievalService(assetRepository, storageService, eventDispatcher);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void findById_usesTenantScopedLookupAndReturnsEmptyForOtherTenant() {
    String tenantId = "tenant-a";
    TenantContext.setCurrentTenant(tenantId);

    when(assetRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, 42L))
        .thenReturn(Optional.empty());

    assertThat(retrievalService.findById(42L)).isEmpty();
  }

  @Test
  void getDownloadUrl_crossTenantAssetReturns404() {
    String tenantId = "tenant-a";
    TenantContext.setCurrentTenant(tenantId);

    Asset foreignAsset =
        Asset.create(
            "tenant-b",
            "name",
            "file.txt",
            "text/plain",
            10L,
            "source",
            AssetType.OTHER,
            "/folder",
            false);

    when(assetRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, 99L))
        .thenReturn(Optional.of(foreignAsset));

    assertThatThrownBy(() -> retrievalService.getDownloadUrl(99L, "user", "127.0.0.1"))
        .isInstanceOf(AssetException.class)
        .hasMessageContaining("Asset not found")
        .extracting("errorCode")
        .isEqualTo(AssetErrorCode.ASSET_NOT_FOUND);
  }
}
