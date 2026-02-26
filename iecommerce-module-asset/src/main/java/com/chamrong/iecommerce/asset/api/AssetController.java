package com.chamrong.iecommerce.asset.api;

import com.chamrong.iecommerce.asset.application.AssetService;
import com.chamrong.iecommerce.asset.application.dto.AssetResponse;
import com.chamrong.iecommerce.asset.domain.AssetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Media/asset management — upload, retrieve, and delete files.
 *
 * <p>Base path: {@code /api/v1/assets}
 */
@Tag(name = "Assets", description = "Media file upload and management (images, documents, videos)")
@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AssetController {

  private final AssetService assetService;

  @Operation(
      summary = "Upload a file",
      description =
          "Accepts a multipart file and stores it. Returns the asset record with a source URL.")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAuthority('assets:manage')")
  public ResponseEntity<AssetResponse> upload(
      @RequestParam String tenantId,
      @RequestParam("file") MultipartFile file,
      @RequestParam(defaultValue = "IMAGE") AssetType type,
      @RequestParam(required = false) String path) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(assetService.upload(tenantId, file, type, path));
  }

  @Operation(summary = "Get asset by ID")
  @GetMapping("/{id}")
  public ResponseEntity<AssetResponse> getById(@PathVariable Long id) {
    return assetService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "List assets by type")
  @GetMapping
  public List<AssetResponse> listByType(@RequestParam(defaultValue = "IMAGE") AssetType type) {
    return assetService.findByType(type);
  }

  @Operation(summary = "Delete an asset")
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('assets:manage')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    assetService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
