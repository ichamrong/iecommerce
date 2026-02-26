package com.chamrong.iecommerce.asset.domain.exception;

import lombok.Getter;

/** Base exception for all Asset module domain errors. */
@Getter
public class AssetException extends RuntimeException {

  private final AssetErrorCode errorCode;

  public AssetException(AssetErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public AssetException(AssetErrorCode errorCode, String detail) {
    super(errorCode.getMessage() + ": " + detail);
    this.errorCode = errorCode;
  }

  public AssetException(AssetErrorCode errorCode, Throwable cause) {
    super(errorCode.getMessage(), cause);
    this.errorCode = errorCode;
  }
}
