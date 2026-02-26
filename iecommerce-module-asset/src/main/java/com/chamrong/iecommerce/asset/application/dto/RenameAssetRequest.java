package com.chamrong.iecommerce.asset.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameAssetRequest(@NotBlank(message = "New name cannot be blank") String newName) {}
