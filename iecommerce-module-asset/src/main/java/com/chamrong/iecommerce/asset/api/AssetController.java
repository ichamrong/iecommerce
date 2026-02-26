package com.chamrong.iecommerce.asset.api;

import com.chamrong.iecommerce.asset.application.AssetService;
import com.chamrong.iecommerce.asset.application.dto.AssetResponse;
import com.chamrong.iecommerce.asset.application.dto.BulkAssetRequest;
import com.chamrong.iecommerce.asset.application.dto.BulkMoveAssetRequest;
import com.chamrong.iecommerce.asset.application.dto.CreateFolderRequest;
import com.chamrong.iecommerce.asset.application.dto.RenameAssetRequest;
import com.chamrong.iecommerce.asset.application.dto.UploadAssetMetadata;
import com.chamrong.iecommerce.asset.domain.AssetType;
import com.chamrong.iecommerce.asset.domain.StorageConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Media/asset management — upload, retrieve, and delete files.
 *
 * <p>Base path: {@code /api/v1/assets}
 */
@Tag(name = "Assets", description = "Media file upload and management (images, documents, videos)")
@RestController
@RequestMapping(StorageConstants.API_V1_ASSETS)
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
      @RequestPart("file") MultipartFile file,
      @Valid @RequestPart("metadata") UploadAssetMetadata metadata) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(assetService.upload(file, metadata.type(), metadata.path()));
  }

  @Operation(summary = "Search assets by name")
  @GetMapping("/search/name")
  @PreAuthorize("hasAuthority('assets:read')")
  public ResponseEntity<List<AssetResponse>> searchByName(@RequestParam String query) {
    return ResponseEntity.ok(assetService.searchByName(query));
  }

  @Operation(summary = "Search assets by size range")
  @GetMapping("/search/size")
  @PreAuthorize("hasAuthority('assets:read')")
  public ResponseEntity<List<AssetResponse>> searchBySize(
      @RequestParam long minSize, @RequestParam long maxSize) {
    return ResponseEntity.ok(assetService.searchBySize(minSize, maxSize));
  }

  @Operation(summary = "Get asset by ID")
  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('assets:read')")
  public ResponseEntity<AssetResponse> getById(@PathVariable Long id) {
    return assetService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Securely download an asset (redirects to pre-signed URL)")
  @GetMapping("/{id}" + StorageConstants.DOWNLOAD_SUFFIX)
  @PreAuthorize("hasAuthority('assets:read')")
  public ResponseEntity<Void> download(
      @PathVariable Long id, HttpServletRequest request, Authentication authentication) {
    String requestedBy = authentication.getName();
    String ipAddress = request.getRemoteAddr();

    String downloadUrl = assetService.getDownloadUrl(id, requestedBy, ipAddress);

    return ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.LOCATION, downloadUrl)
        .build();
  }

  @Operation(summary = "Proxy download of an asset (streams directly through backend)")
  @GetMapping("/proxy/{id}")
  @PreAuthorize("hasAuthority('assets:read')")
  public ResponseEntity<org.springframework.core.io.Resource> proxyDownload(@PathVariable Long id) {
    AssetResponse asset =
        assetService
            .findById(id)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Asset not found"));

    java.io.InputStream inputStream = assetService.download(id);
    org.springframework.core.io.InputStreamResource resource =
        new org.springframework.core.io.InputStreamResource(inputStream);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\""
                + asset.fileName()
                + "\"; filename*=UTF-8''"
                + asset.fileName())
        .header(StorageConstants.HEADER_X_CONTENT_TYPE_OPTIONS, StorageConstants.VALUE_NOSNIFF)
        .contentType(MediaType.parseMediaType(asset.mimeType()))
        .body(resource);
  }

  @Operation(summary = "Create a virtual folder")
  @PostMapping("/folder")
  @PreAuthorize("hasAuthority('assets:manage')")
  public ResponseEntity<AssetResponse> createFolder(
      @Valid @RequestBody CreateFolderRequest request) {
    AssetResponse response =
        assetService.createFolder(request.parentId(), request.name(), request.type());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Copy an asset to a new folder")
  @PostMapping("/{id}/copy")
  @PreAuthorize("hasAuthority('assets:manage')")
  public ResponseEntity<AssetResponse> copyAsset(
      @PathVariable Long id, @RequestParam(required = false) Long targetFolderId) {
    AssetResponse response = assetService.copyAsset(id, targetFolderId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Move an asset to a new folder")
  @PutMapping("/{id}/move")
  @PreAuthorize("hasAuthority('assets:manage')")
  public ResponseEntity<AssetResponse> moveAsset(
      @PathVariable Long id, @RequestParam(required = false) Long targetFolderId) {
    AssetResponse response = assetService.moveAsset(id, targetFolderId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Rename an asset")
  @PutMapping("/{id}/rename")
  @PreAuthorize("hasAuthority('assets:manage')")
  public ResponseEntity<AssetResponse> renameAsset(
      @PathVariable Long id, @Valid @RequestBody RenameAssetRequest request) {
    AssetResponse response = assetService.renameAsset(id, request.newName());
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Bulk delete assets")
  @PostMapping("/bulk-delete")
  @PreAuthorize("hasAuthority('assets:manage')")
  public ResponseEntity<Void> bulkDelete(@Valid @RequestBody BulkAssetRequest request) {
    assetService.bulkDelete(request.assetIds());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Bulk move assets to a folder")
  @PutMapping("/bulk-move")
  @PreAuthorize("hasAuthority('assets:manage')")
  public ResponseEntity<List<AssetResponse>> bulkMove(
      @Valid @RequestBody BulkMoveAssetRequest request) {
    List<AssetResponse> responses =
        assetService.bulkMove(request.assetIds(), request.targetFolderId());
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "List assets by type")
  @GetMapping
  @PreAuthorize("hasAuthority('assets:read')")
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
