package com.chamrong.iecommerce.asset.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.asset.application.dto.AssetResponse;
import com.chamrong.iecommerce.asset.application.dto.AssetStreamResponse;
import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.AssetException;
import com.chamrong.iecommerce.asset.domain.exception.SecurityValidationException;
import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.event.AssetAccessedEvent;
import com.chamrong.iecommerce.common.event.EventDispatcher;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
class AssetRetrievalServiceTest {

  @Mock private AssetRepository assetRepository;
  @Mock private StorageService storageService;
  @Mock private EventDispatcher eventDispatcher;

  @InjectMocks private AssetRetrievalService assetRetrievalService;

  private final String tenantId = "tenant-1";
  private Asset mockAsset;

  @BeforeEach
  void setUp() {
    TenantContext.setCurrentTenant(tenantId);
    mockAsset = new Asset();
    mockAsset.setId(10L);
    mockAsset.setTenantId(tenantId);
    mockAsset.setFileName("test.jpg");
    mockAsset.setPublic(true);
    mockAsset.setSource("s3://bucket/test.jpg");
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void getProxyResource_PublicAsset_Success() {
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(mockAsset));
    InputStream mockStream = new ByteArrayInputStream("data".getBytes());
    when(storageService.download("s3://bucket/test.jpg")).thenReturn(mockStream);

    AssetStreamResponse response = assetRetrievalService.getProxyResource(10L, false);

    assertNotNull(response);
    assertEquals(10L, response.asset().id());
    assertEquals(mockStream, response.inputStream());
  }

  @Test
  void getProxyResource_PrivateAsset_Authorized_Success() {
    mockAsset.setPublic(false);
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(mockAsset));
    InputStream mockStream = new ByteArrayInputStream("data".getBytes());
    when(storageService.download("s3://bucket/test.jpg")).thenReturn(mockStream);

    AssetStreamResponse response = assetRetrievalService.getProxyResource(10L, true);

    assertNotNull(response);
  }

  @Test
  void getProxyResource_PrivateAsset_Unauthorized_ThrowsException() {
    mockAsset.setPublic(false);
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(mockAsset));

    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> assetRetrievalService.getProxyResource(10L, false));

    assertEquals(AssetErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
  }

  @Test
  void getProxyResource_WrongTenant_ThrowsAccessDenied() {
    mockAsset.setTenantId("different-tenant");
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(mockAsset));

    assertThrows(
        AccessDeniedException.class, () -> assetRetrievalService.getProxyResource(10L, true));
  }

  @Test
  void searchByName_Success() {
    when(assetRepository.findByTenantIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(
            tenantId, "test"))
        .thenReturn(List.of(mockAsset));

    List<AssetResponse> results = assetRetrievalService.searchByName("test");

    assertEquals(1, results.size());
    assertEquals(10L, results.get(0).id());
  }

  @Test
  void getDownloadUrl_Success() {
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(mockAsset));
    when(storageService.getPublicUrl("s3://bucket/test.jpg")).thenReturn("http://cdn.com/test.jpg");

    String url = assetRetrievalService.getDownloadUrl(10L, "user1", "127.0.0.1");

    assertEquals("http://cdn.com/test.jpg", url);
    verify(eventDispatcher).dispatch(any(AssetAccessedEvent.class));
  }

  @Test
  void download_Success() {
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(mockAsset));
    InputStream mockStream = new ByteArrayInputStream("data".getBytes());
    when(storageService.download("s3://bucket/test.jpg")).thenReturn(mockStream);

    InputStream result = assetRetrievalService.download(10L);

    assertEquals(mockStream, result);
  }

  @Test
  void download_NotFound() {
    when(assetRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.empty());

    AssetException exception =
        assertThrows(AssetException.class, () -> assetRetrievalService.download(10L));
    assertEquals(AssetErrorCode.ASSET_NOT_FOUND, exception.getErrorCode());
  }
}
