package com.chamrong.iecommerce.asset.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.chamrong.iecommerce.asset.domain.StorageProvider;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.common.event.EventDispatcher;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
class StorageRoutingServiceTest {

  @Mock private StorageService mockProvider;
  @Mock private StorageRoutingConfiguration config;
  @Mock private EventDispatcher eventDispatcher;
  @Mock private CacheManager cacheManager;

  private StorageRoutingService routingService;

  @BeforeEach
  void setUp() {
    when(mockProvider.getProviderName()).thenReturn("r2");
    routingService =
        new StorageRoutingService(
            Arrays.asList(mockProvider), config, eventDispatcher, cacheManager);
    routingService.init();
  }

  @Test
  void testProviderRegistration_AuditedAndCached() {
    when(config.getProvider()).thenReturn(StorageProvider.R2);

    // The service returned by routingService should be wrapped
    // If I call a method, it should go through the decorators to the mockProvider
    routingService.upload("test.txt", "text/plain", null, 0);
    verify(mockProvider, times(1)).upload(anyString(), anyString(), any(), anyLong());
  }

  @Test
  void testAliasRegistration_MappingCorrect() {
    // R2 provider has 's3' as alias in StorageProvider enum
    // StorageRoutingService should have registered both 'r2' and 's3'
    when(config.getProvider()).thenReturn(StorageProvider.fromKey("s3"));

    // This should route to mockProvider (registered as 'r2')
    routingService.delete("alias-source");
    verify(mockProvider, times(1)).delete("alias-source");
  }

  @Test
  void testGcsAliasRegistration_MappingCorrect() {
    StorageService gcsMock = mock(StorageService.class);
    when(gcsMock.getProviderName()).thenReturn("gcs");

    StorageRoutingService gcsRoutingService =
        new StorageRoutingService(Arrays.asList(gcsMock), config, eventDispatcher, cacheManager);
    gcsRoutingService.init();

    // GCS provider has 'google' as alias
    when(config.getProvider()).thenReturn(StorageProvider.fromKey("google"));

    gcsRoutingService.delete("google-source");
    verify(gcsMock, times(1)).delete("google-source");
  }

  @Test
  void testFallbackMechanism() {
    when(config.getProvider()).thenReturn(StorageProvider.GCS); // Not registered in init

    // Should fallback to default (R2 in StorageConstants)
    routingService.delete("some-source");
    verify(mockProvider, times(1)).delete("some-source");
  }
}
