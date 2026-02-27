package com.chamrong.iecommerce.asset.domain;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public interface StorageService {

  /** Uploads a file and returns the unique source/key. */
  String upload(String fileName, String contentType, InputStream inputStream, long size);

  /** Generates a public URL for the asset. */
  String getPublicUrl(String source);

  /** Generates a temporary pre-signed URL for direct download, if supported by provider. */
  default Optional<String> generatePresignedUrl(String source) {
    return Optional.empty();
  }

  // --- Multipart Upload Support ---

  /** Initiates a multipart upload and returns an upload ID. */
  default String initiateMultipartUpload(String fileName, String contentType) {
    throw new UnsupportedOperationException("Multipart upload not supported by this provider");
  }

  /** Uploads a single part/chunk of a multipart upload. Returns the part's ETag. */
  default String uploadPart(
      String uploadId, String key, int partNumber, InputStream inputStream, long size) {
    throw new UnsupportedOperationException("Multipart upload not supported by this provider");
  }

  /** Completes a multipart upload using the accumulated part ETags. */
  default String completeMultipartUpload(String uploadId, String key, Map<Integer, String> parts) {
    throw new UnsupportedOperationException("Multipart upload not supported by this provider");
  }

  /** Aborts a multipart upload and cleans up uploaded parts. */
  default void abortMultipartUpload(String uploadId, String key) {
    throw new UnsupportedOperationException("Multipart upload not supported by this provider");
  }

  void delete(String source);

  /** Copies an object from a source to a specific destination path. Returns the new source. */
  String copy(String source, String destination);

  /** Moves an object to a new destination path. Returns the new source. */
  String move(String source, String destination);

  /** Creates a directory structure. Returns the path. */
  String createFolder(String folderPath);

  /** Downloads the file content as an InputStream. */
  InputStream download(String source);

  /** Returns the unique identifier for this storage provider (e.g., "r2", "telegram"). */
  String getProviderName();
}
