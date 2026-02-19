# Module Specification: Asset

## 1. Purpose
The Asset module handles the storage, retrieval, and optimization of all digital media (Images, Videos, PDFs) across the platform.

## 2. Core Domain Models
- **Asset**: The central record for a file.
  - **Bucket**: The Minio bucket name (e.g., `tenant-1-products`).
  - **ObjectKey**: The path in Minio (e.g., `images/2026/room-101-main.jpg`).
  - **MimeType**: e.g., `image/jpeg`.
  - **Metadata**: Title, Alt-text, Dimensions.
- **AssetCollection**: A group of related assets (e.g., "Standard Queen Gallery").

## 3. Storage Strategy: Minio (S3-Compatible)
We use **Minio** as the object storage layer:
- **Scalability**: Can be deployed on-premise or scaled in the cloud perfectly.
- **Security**: Permanent storage is private; assets are served via **Pre-signed URLs** for security.
- **Optimization**: All uploaded images are automatically resized and converted to **WebP** for faster page loads.

## 4. Multi-Tenancy Strategy
- **Bucket Partitioning**: Every tenant has their own prefix or dedicated bucket in Minio.
- **Privacy**: Access is controlled via tenant-specific credentials or temporary tokens.

## 5. Public APIs (Internal Modulith)
- `AssetService.upload(InputStream, filename, tenantId)`: Returns the Asset ID.
- `AssetService.getPublicUrl(assetId)`: Generates a temporary pre-signed URL.
- `AssetService.delete(assetId)`: Clean up storage.
