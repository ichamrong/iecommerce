package com.chamrong.iecommerce.asset.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BulkMoveAssetRequest(
    @NotEmpty(message = "Asset IDs list cannot be empty") List<Long> assetIds,
    @NotNull(message = "Target folder ID is required") Long targetFolderId) {}
