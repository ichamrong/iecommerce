package com.chamrong.iecommerce.asset.domain;

import java.io.InputStream;

public interface StorageService {

  /** Uploads a file and returns the unique source/key. */
  String upload(String fileName, String contentType, InputStream inputStream, long size);

  /** Generates a public URL for the asset. */
  String getPublicUrl(String source);

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
