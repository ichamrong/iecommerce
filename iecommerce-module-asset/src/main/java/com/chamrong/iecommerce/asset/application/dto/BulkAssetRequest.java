package com.chamrong.iecommerce.asset.application.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkAssetRequest(
    @NotEmpty(message = "Asset IDs list cannot be empty") List<Long> assetIds) {}
