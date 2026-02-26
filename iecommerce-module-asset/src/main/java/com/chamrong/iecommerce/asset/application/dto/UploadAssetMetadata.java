package com.chamrong.iecommerce.asset.application.dto;

import com.chamrong.iecommerce.asset.domain.AssetType;
import jakarta.validation.constraints.NotNull;

public record UploadAssetMetadata(
    @NotNull(message = "Asset type is required") AssetType type, String path) {}
