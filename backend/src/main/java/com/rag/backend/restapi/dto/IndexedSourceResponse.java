package com.rag.backend.restapi.dto;

import java.time.Instant;

public record IndexedSourceResponse(
        String url,
        long chunks,
        Instant indexedAt
) {
}
