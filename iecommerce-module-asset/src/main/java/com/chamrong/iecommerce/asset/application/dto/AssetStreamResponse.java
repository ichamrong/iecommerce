package com.chamrong.iecommerce.asset.application.dto;

import java.io.InputStream;

/** Result object for proxy downloads containing both asset metadata and the data stream. */
public record AssetStreamResponse(AssetResponse asset, InputStream inputStream) {}
