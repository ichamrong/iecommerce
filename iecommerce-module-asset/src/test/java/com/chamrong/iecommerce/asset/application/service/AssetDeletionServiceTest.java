package com.chamrong.iecommerce.asset.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.AssetException;
import com.chamrong.iecommerce.common.TenantContext;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class AssetDeletionServiceTest {

  @Mock private AssetRepository assetRepository;
  @Mock private StorageService storageService;

  @InjectMocks private AssetDeletionService assetDeletionService;

  private final String tenantId = "tenant-1";
  private Asset mockAsset;
  private Asset mockFolder;

  @BeforeEach
  void setUp() {
    TenantContext.setCurrentTenant(tenantId);

    mockAsset = new Asset();
    mockAsset.setId(10L);
    mockAsset.setTenantId(tenantId);
    mockAsset.setFileName("test.jpg");
    mockAsset.setFolder(false);
    mockAsset.setPath("/test.jpg");
    mockAsset.setSource("s3://bucket/test.jpg");

    mockFolder = new Asset();
    mockFolder.setId(20L);
    mockFolder.setTenantId(tenantId);
    mockFolder.setFileName("folder1");
    mockFolder.setFolder(true);
    mockFolder.setPath("/folder1");
    mockFolder.setSource("s3://bucket/folder1/");
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void delete_SingleFile_Success() {
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(mockAsset));

    assetDeletionService.delete(10L);

    verify(storageService).delete("s3://bucket/test.jpg");
    verify(assetRepository).save(mockAsset);
    assertNotNull(mockAsset.getDeletedAt());
  }

  @Test
  void delete_Folder_Success() {
    when(assetRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(mockFolder));

    assetDeletionService.delete(20L);

    verify(assetRepository).deleteByTenantIdAndPathStartingWith(eq(tenantId), eq("/folder1/"));
    verify(assetRepository).save(mockFolder);
    verifyNoInteractions(storageService);
    assertNotNull(mockFolder.getDeletedAt());
  }

  @Test
  void bulkDelete_Success() {
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(mockAsset));
    when(assetRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(mockFolder));

    assetDeletionService.bulkDelete(List.of(10L, 20L));

    verify(storageService).delete("s3://bucket/test.jpg");
    verify(assetRepository).deleteByTenantIdAndPathStartingWith(eq(tenantId), eq("/folder1/"));
    verify(assetRepository).saveAll(anyIterable());
    assertNotNull(mockAsset.getDeletedAt());
    assertNotNull(mockFolder.getDeletedAt());
  }

  @Test
  void bulkDelete_EmptyList() {
    assetDeletionService.bulkDelete(List.of());

    verifyNoInteractions(assetRepository, storageService);
  }

  @Test
  void bulkDelete_NoneFound_ThrowsException() {
    when(assetRepository.findByIdAndDeletedAtIsNull(88L)).thenReturn(Optional.empty());

    AssetException exception =
        assertThrows(AssetException.class, () -> assetDeletionService.bulkDelete(List.of(88L)));
    assertEquals(AssetErrorCode.ASSET_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void delete_NotFound_ThrowsException() {
    when(assetRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

    AssetException exception =
        assertThrows(AssetException.class, () -> assetDeletionService.delete(99L));
    assertEquals(AssetErrorCode.ASSET_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void delete_WrongTenant_ThrowsAccessDenied() {
    mockAsset.setTenantId("different-tenant");
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(mockAsset));

    assertThrows(AccessDeniedException.class, () -> assetDeletionService.delete(10L));
  }
}
