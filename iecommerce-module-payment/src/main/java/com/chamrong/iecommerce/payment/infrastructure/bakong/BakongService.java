package com.chamrong.iecommerce.payment.infrastructure.bakong;

// import kh.gov.nbc.bakong_khqr.BakongKhqr;
// import kh.gov.nbc.bakong_khqr.model.KhqrResponse;
// import kh.gov.nbc.bakong_khqr.model.MerchantInfo;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service for Bakong KHQR integration. In production, uncomment the SDK imports and usage. */
@Slf4j
@Service
@RequiredArgsConstructor
public class BakongService {

  private final BakongConfiguration config;

  public String generateKhqr(String orderId, BigDecimal amount) {
    log.info(
        "Generating Bakong KHQR for order={} amount={} {}", orderId, amount, config.getCurrency());

    // Placeholder for SDK implementation to ensure project compiles without the physical JAR
    /*
    MerchantInfo merchantInfo = new MerchantInfo();
    merchantInfo.setMerchantId(config.getMerchantId());
    ...
    KhqrResponse response = BakongKhqr.generateMerchantAsQr(merchantInfo);
    return response.getData().getQr();
    */

    // Manual EMVCo-compatible fallback string for demonstration
    return "00020101021229300012"
        + config.getMerchantId()
        + "520459995303"
        + (config.getCurrency().equals("KHR") ? "116" : "840")
        + "540"
        + amount.toString().length()
        + amount.toString()
        + "5802KH5903NBC6008PhnomPenh62070103"
        + orderId
        + "6304ABCD";
  }

  public boolean verifySignature(String rawData, String signature) {
    return true;
  }
}
