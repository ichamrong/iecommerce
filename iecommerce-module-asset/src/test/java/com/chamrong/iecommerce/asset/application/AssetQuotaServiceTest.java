package com.chamrong.iecommerce.asset.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.asset.application.service.AssetQuotaService;
import com.chamrong.iecommerce.setting.application.QuotaEnforcer;
import com.chamrong.iecommerce.setting.domain.SettingKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssetQuotaServiceTest {

  @Mock private QuotaEnforcer quotaEnforcer;

  @Test
  void getQuotaForTenant_usesConfiguredMediaQuotaInMb() {
    AssetQuotaService service = new AssetQuotaService(quotaEnforcer);
    when(quotaEnforcer.getLimit("tenant-1", SettingKeys.QUOTA_MAX_MEDIA_MB)).thenReturn(1024);

    long quota = service.getQuotaForTenant("tenant-1");

    assertThat(quota).isEqualTo(1024L * 1024L * 1024L);
  }

  @Test
  void getQuotaForTenant_unlimitedWhenMaxValue() {
    AssetQuotaService service = new AssetQuotaService(quotaEnforcer);
    when(quotaEnforcer.getLimit("tenant-1", SettingKeys.QUOTA_MAX_MEDIA_MB))
        .thenReturn(Integer.MAX_VALUE);

    long quota = service.getQuotaForTenant("tenant-1");

    assertThat(quota).isEqualTo(Long.MAX_VALUE);
  }
}
