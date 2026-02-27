package com.chamrong.iecommerce.asset.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiateMultipartUploadResponse {
  private String uploadId;
  private String key;
}
