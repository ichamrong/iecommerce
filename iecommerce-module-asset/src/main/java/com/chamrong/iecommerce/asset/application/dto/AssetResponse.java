package com.chamrong.iecommerce.asset.application.dto;

import java.time.Instant;

public record AssetResponse(
    Long id,
    String name,
    String fileName,
    String mimeType,
    Long fileSize,
    String source,
    String url,
    String type,
    Instant createdAt) {}
