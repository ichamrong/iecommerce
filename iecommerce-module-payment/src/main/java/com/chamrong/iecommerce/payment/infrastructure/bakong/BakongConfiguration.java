package com.chamrong.iecommerce.payment.infrastructure.bakong;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "iecommerce.payment.bakong")
public class BakongConfiguration {
  private String bankName;
  private String merchantName;
  private String merchantCity;
  private String merchantId;
  private String accountId;
  private String acquiringBank;
  private String currency; // KHR or USD

  public String getBankName() {
    return bankName;
  }

  public void setBankName(String bankName) {
    this.bankName = bankName;
  }

  public String getMerchantName() {
    return merchantName;
  }

  public void setMerchantName(String merchantName) {
    this.merchantName = merchantName;
  }

  public String getMerchantCity() {
    return merchantCity;
  }

  public void setMerchantCity(String merchantCity) {
    this.merchantCity = merchantCity;
  }

  public String getMerchantId() {
    return merchantId;
  }

  public void setMerchantId(String merchantId) {
    this.merchantId = merchantId;
  }

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getAcquiringBank() {
    return acquiringBank;
  }

  public void setAcquiringBank(String acquiringBank) {
    this.acquiringBank = acquiringBank;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }
}
