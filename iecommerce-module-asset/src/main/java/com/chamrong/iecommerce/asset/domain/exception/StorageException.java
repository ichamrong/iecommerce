package com.chamrong.iecommerce.asset.domain.exception;

/** Thrown when a storage backend operation fails or the backend is unreachable. */
public class StorageException extends AssetException {

  public StorageException(String detail) {
    super(AssetErrorCode.STORAGE_OPERATION_FAILED, detail);
  }

  public StorageException(Throwable cause) {
    super(AssetErrorCode.STORAGE_OPERATION_FAILED, cause);
  }

  public StorageException(AssetErrorCode errorCode, String detail) {
    super(errorCode, detail);
  }
}
