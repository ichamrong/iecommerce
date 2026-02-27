package com.chamrong.iecommerce.asset.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.asset.application.dto.AssetResponse;
import com.chamrong.iecommerce.asset.application.dto.UploadAssetMetadata;
import com.chamrong.iecommerce.asset.application.metadata.CompositeMetadataExtractor;
import com.chamrong.iecommerce.asset.application.processing.ImageProcessingService;
import com.chamrong.iecommerce.asset.application.security.FileScanner;
import com.chamrong.iecommerce.asset.application.security.FileSecurityValidator;
import com.chamrong.iecommerce.asset.application.service.AssetUploadService;
import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.AssetType;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.AssetException;
import com.chamrong.iecommerce.asset.domain.exception.SecurityValidationException;
import com.chamrong.iecommerce.asset.domain.exception.StorageException;
import com.chamrong.iecommerce.common.TenantContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class AssetUploadServiceTest {

  @Mock private AssetRepository assetRepository;
  @Mock private StorageService storageService;
  @Mock private ImageProcessingService imageProcessingService;
  @Mock private FileSecurityValidator fileSecurityValidator;
  @Mock private FileScanner fileScanner;
  @Mock private CompositeMetadataExtractor metadataExtractor;

  @InjectMocks private AssetUploadService assetUploadService;

  private final String tenantId = "tenant-1";
  private final String originalFilename = "test-image.jpg";
  private final String mimeType = "image/jpeg";
  private final byte[] content = "fake-image-content".getBytes();
  private MockMultipartFile mockFile;
  private UploadAssetMetadata mockMetadata;

  @BeforeEach
  void setUp() {
    TenantContext.setCurrentTenant(tenantId);
    mockFile = new MockMultipartFile("file", originalFilename, mimeType, content);
    mockMetadata =
        new UploadAssetMetadata(
            AssetType.IMAGE, "/test-folder", null, null, false, false, false, true);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void upload_Success() {
    Asset savedAsset = new Asset();
    savedAsset.setId(1L);
    savedAsset.setName(originalFilename);
    savedAsset.setTenantId(tenantId);

    when(assetRepository.sumFileSizeByTenantIdAndDeletedAtIsNull(tenantId)).thenReturn(0L);
    when(storageService.upload(
            eq(originalFilename), eq(mimeType), any(InputStream.class), eq((long) content.length)))
        .thenReturn("s3://bucket/path/test-image.jpg");
    when(metadataExtractor.extract(any(InputStream.class), eq(mimeType)))
        .thenReturn(Collections.emptyMap());
    when(assetRepository.save(any(Asset.class))).thenReturn(savedAsset);

    AssetResponse response = assetUploadService.upload(mockFile, mockMetadata);

    assertNotNull(response);
    assertEquals(1L, response.id());
    verify(fileSecurityValidator).validate(eq(originalFilename), any(InputStream.class));
    verify(fileScanner).scan(any(InputStream.class), eq(originalFilename));
    verify(assetRepository).save(any(Asset.class));
  }

  @Test
  void upload_QuotaExceeded() {
    long currentUsage = 5L * 1024 * 1024 * 1024; // 5GB limit reached

    when(assetRepository.sumFileSizeByTenantIdAndDeletedAtIsNull(tenantId))
        .thenReturn(currentUsage);

    AssetException exception =
        assertThrows(AssetException.class, () -> assetUploadService.upload(mockFile, mockMetadata));
    assertEquals(AssetErrorCode.STORAGE_QUOTA_EXCEEDED, exception.getErrorCode());
    verifyNoInteractions(storageService, fileSecurityValidator);
  }

  @Test
  void upload_SecurityValidationException() {
    when(assetRepository.sumFileSizeByTenantIdAndDeletedAtIsNull(tenantId)).thenReturn(0L);
    doThrow(new SecurityValidationException(AssetErrorCode.INSECURE_FILE, "Bad file"))
        .when(fileSecurityValidator)
        .validate(anyString(), any(InputStream.class));

    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> assetUploadService.upload(mockFile, mockMetadata));
    assertEquals(AssetErrorCode.INSECURE_FILE, exception.getErrorCode());
    verifyNoInteractions(storageService);
  }

  @Test
  void upload_StorageException() {
    when(assetRepository.sumFileSizeByTenantIdAndDeletedAtIsNull(tenantId)).thenReturn(0L);
    when(storageService.upload(anyString(), anyString(), any(InputStream.class), anyLong()))
        .thenThrow(new StorageException(AssetErrorCode.STORAGE_OPERATION_FAILED, "S3 down"));

    AssetException exception =
        assertThrows(AssetException.class, () -> assetUploadService.upload(mockFile, mockMetadata));
    assertEquals(AssetErrorCode.STORAGE_OPERATION_FAILED, exception.getErrorCode());
  }

  @Test
  void upload_withImageProcessing_Success() throws Exception {
    // Setup metadata for resize
    UploadAssetMetadata resizeMetadata =
        new UploadAssetMetadata(
            AssetType.IMAGE, "/test-folder", 100, 100, false, false, false, true);

    Asset savedAsset = new Asset();
    savedAsset.setId(1L);
    savedAsset.setTenantId(tenantId);

    when(assetRepository.sumFileSizeByTenantIdAndDeletedAtIsNull(tenantId)).thenReturn(0L);

    InputStream resizedStream = new ByteArrayInputStream("resized-content".getBytes());
    when(imageProcessingService.resize(any(InputStream.class), eq(100), eq(100), eq("jpeg")))
        .thenReturn(resizedStream);

    when(storageService.upload(
            eq(originalFilename),
            eq(mimeType),
            eq(resizedStream),
            eq((long) "resized-content".length())))
        .thenReturn("s3://bucket/path/resized.jpg");
    when(metadataExtractor.extract(any(InputStream.class), eq(mimeType)))
        .thenReturn(Collections.emptyMap());
    when(assetRepository.save(any(Asset.class))).thenReturn(savedAsset);

    AssetResponse response = assetUploadService.upload(mockFile, resizeMetadata);

    assertNotNull(response);
    verify(imageProcessingService).resize(any(InputStream.class), eq(100), eq(100), eq("jpeg"));
    verify(storageService)
        .upload(
            anyString(),
            anyString(),
            any(InputStream.class),
            eq((long) "resized-content".length()));
  }
}
