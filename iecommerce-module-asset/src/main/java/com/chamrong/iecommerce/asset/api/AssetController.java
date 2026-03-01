package com.chamrong.iecommerce.asset.api;

import com.chamrong.iecommerce.asset.application.dto.AssetResponse;
import com.chamrong.iecommerce.asset.application.dto.AssetStreamResponse;
import com.chamrong.iecommerce.asset.application.dto.BulkAssetRequest;
import com.chamrong.iecommerce.asset.application.dto.BulkMoveAssetRequest;
import com.chamrong.iecommerce.asset.application.dto.BulkRenameAssetRequest;
import com.chamrong.iecommerce.asset.application.dto.CompleteMultipartUploadRequest;
import com.chamrong.iecommerce.asset.application.dto.CreateFolderRequest;
import com.chamrong.iecommerce.asset.application.dto.InitiateMultipartUploadRequest;
import com.chamrong.iecommerce.asset.application.dto.InitiateMultipartUploadResponse;
import com.chamrong.iecommerce.asset.application.dto.RenameAssetRequest;
import com.chamrong.iecommerce.asset.application.dto.UploadAssetMetadata;
import com.chamrong.iecommerce.asset.application.service.AssetChunkedUploadService;
import com.chamrong.iecommerce.asset.application.service.AssetDeletionService;
import com.chamrong.iecommerce.asset.application.service.AssetRetrievalService;
import com.chamrong.iecommerce.asset.application.service.AssetTransferService;
import com.chamrong.iecommerce.asset.application.service.AssetUploadService;
import com.chamrong.iecommerce.asset.domain.AssetSecurityConstants;
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
import org.springframework.lang.Nullable;
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
public class AssetController {

  private final AssetUploadService assetUploadService;
  private final AssetChunkedUploadService assetChunkedUploadService;
  private final AssetRetrievalService assetRetrievalService;
  private final AssetTransferService assetTransferService;
  private final AssetDeletionService assetDeletionService;

  @Operation(
      summary = "Upload a file",
      description =
          "Accepts a multipart file and stores it. Returns the asset record with a source URL.")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_MANAGE + "')")
  public ResponseEntity<AssetResponse> upload(
      @RequestPart("file") MultipartFile file,
      @Valid @RequestPart("metadata") UploadAssetMetadata metadata) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(assetUploadService.upload(file, metadata));
  }

  @Operation(summary = "Initiate a multipart upload")
  @PostMapping("/multipart")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_MANAGE + "')")
  public ResponseEntity<InitiateMultipartUploadResponse> initiateMultipartUpload(
      @Valid @RequestBody InitiateMultipartUploadRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(assetChunkedUploadService.initiateMultipartUpload(request));
  }

  @Operation(summary = "Upload a part for a multipart upload")
  @PutMapping(
      value = "/multipart/{uploadId}/parts/{partNumber}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_MANAGE + "')")
  public ResponseEntity<String> uploadPart(
      @PathVariable String uploadId,
      @PathVariable int partNumber,
      @RequestParam("key") String key,
      @RequestPart("file") MultipartFile file)
      throws java.io.IOException {
    String eTag =
        assetChunkedUploadService.uploadPart(
            uploadId, key, partNumber, file.getInputStream(), file.getSize());
    return ResponseEntity.ok(eTag);
  }

  @Operation(summary = "Complete a multipart upload")
  @PostMapping("/multipart/{uploadId}/complete")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_MANAGE + "')")
  public ResponseEntity<AssetResponse> completeMultipartUpload(
      @PathVariable String uploadId, @Valid @RequestBody CompleteMultipartUploadRequest request) {
    if (!uploadId.equals(request.getUploadId())) {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok(assetChunkedUploadService.completeMultipartUpload(request));
  }

  @Operation(summary = "Abort a multipart upload")
  @DeleteMapping("/multipart/{uploadId}")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_MANAGE + "')")
  public ResponseEntity<Void> abortMultipartUpload(
      @PathVariable String uploadId, @RequestParam("key") String key) {
    assetChunkedUploadService.abortMultipartUpload(uploadId, key);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Search assets by name")
  @GetMapping("/search/name")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_READ + "')")
  public ResponseEntity<List<AssetResponse>> searchByName(@RequestParam String query) {
    return ResponseEntity.ok(assetRetrievalService.searchByName(query));
  }

  @Operation(summary = "Search assets by size range")
  @GetMapping("/search/size")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_READ + "')")
  public ResponseEntity<List<AssetResponse>> searchBySize(
      @RequestParam long minSize, @RequestParam long maxSize) {
    return ResponseEntity.ok(assetRetrievalService.searchBySize(minSize, maxSize));
  }

  @Operation(summary = "Get asset by ID")
  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_READ + "')")
  public ResponseEntity<AssetResponse> getById(@PathVariable long id) {
    return assetRetrievalService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Securely download an asset (redirects to pre-signed URL)")
  @GetMapping("/{id}" + StorageConstants.DOWNLOAD_SUFFIX)
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_READ + "')")
  public ResponseEntity<Void> download(
      @PathVariable long id, HttpServletRequest request, Authentication authentication) {
    String requestedBy = authentication.getName();
    String ipAddress = request.getRemoteAddr();

    String downloadUrl = assetRetrievalService.getDownloadUrl(id, requestedBy, ipAddress);

    return ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.LOCATION, downloadUrl)
        .build();
  }

  @Operation(summary = "Proxy download of an asset (streams directly through backend)")
  @GetMapping("/proxy/{id}")
  public ResponseEntity<org.springframework.core.io.Resource> proxyDownload(
      @PathVariable long id, @Nullable Authentication authentication) {
    boolean isAuthorized =
        authentication != null
            && authentication.isAuthenticated()
            && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AssetSecurityConstants.ASSET_READ));

    AssetStreamResponse response = assetRetrievalService.getProxyResource(id, isAuthorized);
    AssetResponse asset = response.asset();

    org.springframework.core.io.InputStreamResource resource =
        new org.springframework.core.io.InputStreamResource(response.inputStream());

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\""
                + asset.fileName()
                + "\"; filename*=UTF-8''"
                + asset.fileName())
        .header(StorageConstants.HEADER_X_CONTENT_TYPE_OPTIONS, StorageConstants.VALUE_NOSNIFF)
        .header(
            StorageConstants.HEADER_CONTENT_SECURITY_POLICY,
            "default-src 'none'; style-src 'self' 'unsafe-inline'; sandbox")
        .header(StorageConstants.HEADER_X_FRAME_OPTIONS, "DENY")
        .contentType(MediaType.parseMediaType(asset.mimeType()))
        .body(resource);
  }

  @Operation(summary = "Create a virtual folder")
  @PostMapping("/folder")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_MANAGE + "')")
  public ResponseEntity<AssetResponse> createFolder(
      @Valid @RequestBody CreateFolderRequest request) {
    AssetResponse response =
        assetTransferService.createFolder(request.parentId(), request.name(), request.type());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Copy an asset to a new folder")
  @PostMapping("/{id}/copy")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_MANAGE + "')")
  public ResponseEntity<AssetResponse> copyAsset(
      @PathVariable long id, @Nullable @RequestParam(required = false) Long targetFolderId) {
    AssetResponse response = assetTransferService.copyAsset(id, targetFolderId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Move an asset to a new folder")
  @PutMapping("/{id}/move")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_MANAGE + "')")
  public ResponseEntity<AssetResponse> moveAsset(
      @PathVariable long id, @Nullable @RequestParam(required = false) Long targetFolderId) {
    AssetResponse response = assetTransferService.moveAsset(id, targetFolderId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Rename an asset")
  @PutMapping("/{id}/rename")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_MANAGE + "')")
  public ResponseEntity<AssetResponse> renameAsset(
      @PathVariable long id, @Valid @RequestBody RenameAssetRequest request) {
    AssetResponse response = assetTransferService.renameAsset(id, request.newName());
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Bulk delete assets")
  @PostMapping("/bulk-delete")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_MANAGE + "')")
  public ResponseEntity<Void> bulkDelete(@Valid @RequestBody BulkAssetRequest request) {
    assetDeletionService.bulkDelete(request.assetIds());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Bulk rename assets")
  @PutMapping("/bulk-rename")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_MANAGE + "')")
  public ResponseEntity<List<AssetResponse>> bulkRename(
      @Valid @RequestBody BulkRenameAssetRequest request) {
    List<AssetResponse> responses = assetTransferService.bulkRename(request.assetRenames());
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "Bulk move assets to a folder")
  @PutMapping("/bulk-move")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_MANAGE + "')")
  public ResponseEntity<List<AssetResponse>> bulkMove(
      @Valid @RequestBody BulkMoveAssetRequest request) {
    List<AssetResponse> responses =
        assetTransferService.bulkMove(request.assetIds(), request.targetFolderId());
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "Bulk download assets as ZIP")
  @PostMapping("/bulk-download")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_READ + "')")
  public ResponseEntity<org.springframework.core.io.Resource> bulkDownload(
      @Valid @RequestBody BulkAssetRequest request) {
    java.io.InputStream inputStream = assetRetrievalService.bulkDownload(request.assetIds());
    org.springframework.core.io.InputStreamResource resource =
        new org.springframework.core.io.InputStreamResource(inputStream);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"assets.zip\"")
        .contentType(MediaType.parseMediaType("application/zip"))
        .body(resource);
  }

  @Operation(summary = "List assets by type")
  @GetMapping
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_READ + "')")
  public List<AssetResponse> listByType(@RequestParam(defaultValue = "IMAGE") AssetType type) {
    return assetRetrievalService.findByType(type);
  }

  @Operation(summary = "Delete an asset")
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('" + AssetSecurityConstants.ASSET_MANAGE + "')")
  public ResponseEntity<Void> delete(@PathVariable long id) {
    assetDeletionService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
