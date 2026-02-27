package com.chamrong.iecommerce.asset.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.asset.application.dto.AssetResponse;
import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.AssetType;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.common.TenantContext;
import java.util.List;
import java.util.Map;
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
class AssetTransferServiceTest {

  @Mock private AssetRepository assetRepository;
  @Mock private StorageService storageService;

  @InjectMocks private AssetTransferService assetTransferService;

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
    mockAsset.setName("test.jpg");
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
  void createFolder_RootLevel_Success() {
    when(storageService.createFolder(eq("new-folder/"))).thenReturn("s3://bucket/new-folder/");

    Asset savedFolder = new Asset();
    savedFolder.setId(30L);
    savedFolder.setName("new-folder");
    when(assetRepository.save(any(Asset.class))).thenReturn(savedFolder);

    AssetResponse response =
        assetTransferService.createFolder(null, "new-folder", AssetType.DOCUMENT);

    assertNotNull(response);
    assertEquals(30L, response.id());
    verify(storageService).createFolder("new-folder/");
  }

  @Test
  void createFolder_WithParent_Success() {
    when(assetRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(mockFolder));
    when(storageService.createFolder(eq("s3://bucket/folder1/new-subfolder/")))
        .thenReturn("s3://bucket/folder1/new-subfolder/");

    Asset savedFolder = new Asset();
    savedFolder.setId(30L);
    savedFolder.setName("new-subfolder");
    when(assetRepository.save(any(Asset.class))).thenReturn(savedFolder);

    AssetResponse response =
        assetTransferService.createFolder(20L, "new-subfolder", AssetType.DOCUMENT);

    assertNotNull(response);
    verify(storageService).createFolder("s3://bucket/folder1/new-subfolder/");
  }

  @Test
  void copyAsset_Success() {
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(mockAsset));
    when(assetRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(mockFolder));
    when(storageService.copy(eq("s3://bucket/test.jpg"), eq("s3://bucket/folder1/test.jpg")))
        .thenReturn("s3://bucket/folder1/test.jpg");

    Asset copiedAsset = new Asset();
    copiedAsset.setId(11L);
    copiedAsset.setName("Copy of test.jpg");
    when(assetRepository.save(any(Asset.class))).thenReturn(copiedAsset);

    AssetResponse response = assetTransferService.copyAsset(10L, 20L);

    assertNotNull(response);
    verify(storageService).copy("s3://bucket/test.jpg", "s3://bucket/folder1/test.jpg");
  }

  @Test
  void bulkMove_Success() {
    when(assetRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(mockFolder));
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(mockAsset));
    when(storageService.move(eq("s3://bucket/test.jpg"), eq("s3://bucket/folder1/test.jpg")))
        .thenReturn("s3://bucket/folder1/test.jpg");

    Asset movedAsset = new Asset();
    movedAsset.setId(10L);
    movedAsset.setName("test.jpg");
    when(assetRepository.saveAll(anyIterable())).thenReturn(List.of(movedAsset));

    List<AssetResponse> responses = assetTransferService.bulkMove(List.of(10L), 20L);

    assertEquals(1, responses.size());
    verify(storageService).move("s3://bucket/test.jpg", "s3://bucket/folder1/test.jpg");
    verify(assetRepository).saveAll(anyIterable());
  }

  @Test
  void bulkRename_Success() {
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(mockAsset));

    Asset renamedAsset = new Asset();
    renamedAsset.setId(10L);
    renamedAsset.setName("renamed");
    renamedAsset.setFileName("renamed.jpg");
    when(assetRepository.saveAll(anyIterable())).thenReturn(List.of(renamedAsset));

    List<AssetResponse> responses = assetTransferService.bulkRename(Map.of(10L, "renamed"));

    assertEquals(1, responses.size());
    verify(assetRepository).saveAll(anyIterable());
  }

  @Test
  void copyAsset_Folder_ThrowsException() {
    when(assetRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(mockFolder));

    assertThrows(
        UnsupportedOperationException.class, () -> assetTransferService.copyAsset(20L, null));
  }

  @Test
  void copyAsset_WrongTenant_ThrowsAccessDenied() {
    mockAsset.setTenantId("different-tenant");
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(mockAsset));

    assertThrows(AccessDeniedException.class, () -> assetTransferService.copyAsset(10L, 20L));
  }
}
