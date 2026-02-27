package com.chamrong.iecommerce.asset.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;

@Data
public class CompleteMultipartUploadRequest {
  @NotBlank(message = "Upload ID is required")
  private String uploadId;

  @NotBlank(message = "Key is required")
  private String key;

  @NotEmpty(message = "Parts map cannot be empty")
  private Map<Integer, String> parts;

  @NotNull(message = "Metadata is required")
  private UploadAssetMetadata metadata;
}
