package com.chamrong.iecommerce.asset.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InitiateMultipartUploadRequest {
  @NotBlank(message = "File name is required")
  private String fileName;

  @NotBlank(message = "Content type is required")
  private String contentType;

  @NotNull(message = "Metadata is required")
  private UploadAssetMetadata metadata;
}
