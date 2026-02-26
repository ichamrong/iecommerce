package com.chamrong.iecommerce.asset.application.dto;

import com.chamrong.iecommerce.asset.domain.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFolderRequest(
    @NotBlank(message = "Folder name is required") String name,
    Long parentId,
    @NotNull(message = "Asset type is required") AssetType type) {}
