package com.chamrong.iecommerce.asset.application.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

/** Request for batch renaming assets. */
public record BulkRenameAssetRequest(
    @NotEmpty(message = "Rename map cannot be empty") Map<Long, String> assetRenames) {}
