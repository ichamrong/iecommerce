package com.chamrong.iecommerce.asset.domain.exception;

/** Thrown when a file fails security scanning or validation. */
public class SecurityValidationException extends AssetException {

  public SecurityValidationException(String detail) {
    super(AssetErrorCode.INSECURE_FILE, detail);
  }

  public SecurityValidationException(AssetErrorCode errorCode, String detail) {
    super(errorCode, detail);
  }
}
