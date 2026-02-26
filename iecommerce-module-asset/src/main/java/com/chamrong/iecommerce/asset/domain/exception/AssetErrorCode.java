package com.chamrong.iecommerce.asset.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Type-safe error codes for the Asset module, following bank-level standards for error
 * traceability.
 */
@Getter
@RequiredArgsConstructor
public enum AssetErrorCode {
  // ── Asset Lifecycle Errors ──────────────────────────────────────────────
  ASSET_NOT_FOUND("ASSET-404", "The requested asset could not be found."),
  ASSET_ALREADY_EXISTS("ASSET-409", "An asset with the same name already exists in this location."),
  FOLDER_NOT_EMPTY("ASSET-400", "The folder is not empty and cannot be deleted without recursion."),
  // ── Storage Infrastructure Errors ───────────────────────────────────────
  STORAGE_UNAVAILABLE("STORAGE-503", "The storage backend is currently unavailable."),
  STORAGE_OPERATION_FAILED("STORAGE-500", "A storage backend operation failed unexpectedly."),
  INVALID_STORAGE_PROVIDER(
      "STORAGE-400", "The specified storage provider is invalid or not registered."),
  // ── Security & Validation Errors ────────────────────────────────────────
  INSECURE_FILE(
      "SECURITY-403", "The file failed security validation (dangerous content detected)."),
  INVALID_MIME_TYPE(
      "SECURITY-400", "The file type is not supported or does not match its content."),
  PATH_TRAVERSAL_ATTEMPT("SECURITY-403", "A path traversal attempt was detected."),
  UNAUTHORIZED_ACCESS("SECURITY-401", "You do not have permission to access or manage this asset."),
  // ── Internal System Errors ──────────────────────────────────────────────
  INTERNAL_ERROR("SYS-500", "An internal error occurred in the Asset module."),
  VALIDATION_ERROR("SYS-400", "The request failed validation.");

  private final String code;
  private final String message;
}
