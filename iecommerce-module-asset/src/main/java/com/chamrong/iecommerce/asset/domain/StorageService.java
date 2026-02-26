package com.chamrong.iecommerce.asset.domain;

import java.io.InputStream;

public interface StorageService {
  /** Uploads a file and returns the unique source/key. */
  String upload(String fileName, String contentType, InputStream inputStream, long size);

  /** Generates a public URL for the asset. */
  String getPublicUrl(String source);

  void delete(String source);
}
