package com.chamrong.iecommerce.ekyc.domain;

import com.fasterxml.jackson.annotation.JsonValue;

/** Lifecycle status of an eKYC approval. Serialized as lowercase for API compatibility. */
public enum EkycStatus {
  PENDING("pending"),
  APPROVED("approved"),
  REJECTED("rejected");

  private final String value;

  EkycStatus(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
