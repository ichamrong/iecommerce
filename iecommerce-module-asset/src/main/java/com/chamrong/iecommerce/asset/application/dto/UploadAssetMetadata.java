package com.chamrong.iecommerce.asset.application.dto;

import com.chamrong.iecommerce.asset.domain.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

/** Metadata for asset upload, including optional image processing instructions. */
public record UploadAssetMetadata(
    @NotNull(message = "Asset type is required")
        @Schema(description = "Type of asset", example = "IMAGE")
        AssetType type,
    @Nullable
        @Schema(
            description = "Virtual path in the asset library",
            example = "/products/electronics")
        String path,
    @Nullable @Schema(description = "Target width for image resizing") Integer width,
    @Nullable @Schema(description = "Target height for image resizing") Integer height,
    @Schema(description = "Whether to crop the image to fit dimensions", defaultValue = "false")
        boolean crop,
    @Schema(
            description = "Whether to convert the image to modern WebP format",
            defaultValue = "false")
        boolean convertToWebP,
    @Schema(
            description = "Whether to convert the image to modern AVIF format",
            defaultValue = "false")
        boolean convertToAvif,
    @Schema(description = "Whether the asset is publicly accessible", defaultValue = "false")
        boolean isPublic) {}
